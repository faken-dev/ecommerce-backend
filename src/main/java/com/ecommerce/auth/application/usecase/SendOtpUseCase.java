package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.SendOtpCommand;
import com.ecommerce.auth.application.port.OtpGenerator;
import com.ecommerce.auth.application.port.SpamProtection;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.event.OtpRequestedEvent;
import com.ecommerce.auth.infrastructure.security.OtpHasher;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Send OTP to a user via the specified channel.
 *
 * Spam protection model:
 * - OTP lifespan: always fixed at 5 minutes. Never changes.
 * - Cooldown: mandatory wait before requesting a new OTP.
 *   Attempt 1-2 → 0 min cooldown. Attempt 3+ → (attempts - 2) × 5 min wait.
 *   Example: attempt 3 → 5 min wait, attempt 4 → 10 min wait.
 * - Daily limit: blocks account if daily send count exceeds maxAttempts.
 * - Penalty: extended wait time when spam pattern is detected (rapid repeated requests).
 */
@Service
@RequiredArgsConstructor
public class SendOtpUseCase {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final OtpHasher otpHasher;
    private final EventPublisher eventPublisher;
    private final SpamProtection spamProtection;

    private static final int COOLDOWN_MINUTES = 5;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    @Transactional
    public void execute(SendOtpCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AUTH_INVALID_CREDENTIALS, "User not found"));

        UUID userId = user.getId();

        if (spamProtection.isAccountBlocked(userId)) {
            throw new BusinessException(
                    ErrorCode.AUTH_OTP_BLOCKED,
                    "Account has been suspended due to spam. Please contact support.");
        }

        user.ensureOtpNotBlocked();

        // Increment and check daily send count
        int sendCount = spamProtection.incrementDailySendCount(userId, command.purpose());
        if (sendCount > maxAttempts) {
            spamProtection.blockAccount(userId);
            user.blockAccount();
            user.blockOtp("Daily OTP spam limit reached");
            userRepository.save(user);
            throw new BusinessException(
                    ErrorCode.AUTH_OTP_BLOCKED,
                    "Too many OTP requests today. Account suspended. Please contact support.");
        }

        // Cooldown check: fail fast if user is still in cooldown window
        long cooldownSeconds = calculateCooldown(sendCount);
        long cooldownRemaining = spamProtection.getCooldownRemainingSeconds(userId, command.purpose());
        if (cooldownRemaining > 0) {
            throw new BusinessException(
                    ErrorCode.OTP_SEND_BLOCKED,
                    "Please wait " + formatWaitTime(cooldownRemaining) + " before requesting a new OTP.");
        }

        // Penalty check: detected spam pattern → extend wait
        long penaltyTtl = spamProtection.getPenaltyTtl(userId, command.purpose());
        if (penaltyTtl > 0) {
            int warnCount = spamProtection.getPenaltyWarnCount(userId, command.purpose());
            if (warnCount < 2) {
                spamProtection.incrementPenaltyWarnCount(userId, command.purpose(), penaltyTtl);
            } else {
                long newPenaltyTtl = spamProtection.extendPenalty(
                        userId, command.purpose(), Duration.ofMinutes(COOLDOWN_MINUTES));
                throw new BusinessException(
                        ErrorCode.OTP_SEND_BLOCKED,
                        "Too many requests. Please wait " + formatWaitTime(newPenaltyTtl) + ".");
            }
        }

        // Reject if an active OTP already exists
        Optional<OtpToken> activeToken = otpTokenRepository
                .findLatestActiveByUserIdAndPurpose(userId, command.purpose());
        if (activeToken.isPresent()) {
            // User spamming the button while OTP is still active → enforce cooldown
            spamProtection.setCooldownWindow(userId, command.purpose(), cooldownSeconds);
            throw new BusinessException(
                    ErrorCode.OTP_STILL_ACTIVE,
                    "OTP is still active. Please use the existing code or wait "
                            + formatWaitTime(cooldownSeconds)
                            + " before requesting a new one.");
        }

        // Generate OTP: HMAC-SHA256 hash for storage, raw code cached in Redis for sending
        String rawOtp = otpGenerator.generate(6);
        String otpHash = otpHasher.hash(rawOtp);

        OtpToken otpToken = OtpToken.create(userId, otpHash, command.channel(),
                command.purpose(), otpExpiryMinutes);
        otpTokenRepository.save(otpToken);

        // Cache raw OTP in Redis with TTL = OTP expiry (5 min). Handler reads from here.
        otpTokenRepository.storeRawOtp(userId, command.purpose(), rawOtp, otpExpiryMinutes * 60L);

        // Set cooldown for next request
        long nextCooldownSeconds = calculateCooldown(sendCount + 1);
        if (nextCooldownSeconds > 0) {
            spamProtection.setCooldownWindow(userId, command.purpose(), nextCooldownSeconds);
        }

        // Determine destination
        String destination = switch (command.channel()) {
            case EMAIL -> user.getEmail().value();
            case SMS, WHATSAPP -> user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null;
        };
        if (destination == null || destination.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "No " + command.channel() + " destination found for user.");
        }

        // Publish event (no raw OTP — handler reads from Redis)
        eventPublisher.publish(new OtpRequestedEvent(
                userId, destination, command.channel(), command.purpose(), Instant.now()));
    }

    // cooldownMinutes = max(0, sendCount - 2) × 5
    private long calculateCooldown(int sendCount) {
        return Math.max(0, sendCount - 2) * COOLDOWN_MINUTES * 60L;
    }

    private String formatWaitTime(long seconds) {
        if (seconds < 60)   return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes " + (seconds % 60) + " seconds";
        return (seconds / 3600) + " hours " + ((seconds % 3600) / 60) + " minutes";
    }
}
