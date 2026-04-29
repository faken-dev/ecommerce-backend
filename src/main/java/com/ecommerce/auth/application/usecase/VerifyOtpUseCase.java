package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.VerifyOtpCommand;
import com.ecommerce.auth.application.dto.VerifyOtpResponse;
import com.ecommerce.auth.application.handler.OtpVerificationHandler;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.infrastructure.security.OtpHasher;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyOtpUseCase {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final OtpHasher otpHasher;
    private final EventPublisher eventPublisher;
    private final List<OtpVerificationHandler> handlers;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    @Transactional
    public VerifyOtpResponse execute(VerifyOtpCommand command) {
        return execute(command, false);
    }

    @Transactional
    public VerifyOtpResponse execute(VerifyOtpCommand command, boolean allowUsed) {
        // Load user
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        // Find OTP (Active or recently used if allowed)
        Optional<OtpToken> tokenOpt = allowUsed 
                ? otpTokenRepository.findLatestByUserIdAndPurpose(user.getId(), command.purpose())
                : otpTokenRepository.findLatestActiveByUserIdAndPurpose(user.getId(), command.purpose());

        OtpToken token = tokenOpt.orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        // Validate: expiry + usage + attempt count.
        if (token.isExpired()) throw new BusinessException(ErrorCode.OTP_EXPIRED);
        
        if (token.isUsed() && !allowUsed) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_USED);
        }
        
        // If it was used but more than 5 mins ago, reject it for safety
        if (token.isUsed() && token.getUsedAt().isBefore(Instant.now().minusSeconds(300))) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_USED, "Session expired, please verify again");
        }

        if (!token.isUsed()) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            if (token.getAttemptCount() > maxAttempts) {
                throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
        }

        // Verify code: HMAC-SHA256 hash comparison
        if (!otpHasher.verify(command.code(), token.getCodeHash())) {
            otpTokenRepository.save(token);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        if (!token.isUsed()) {
            token.markAsUsed();
            otpTokenRepository.save(token);
            processUserUpdates(user, command.purpose());
        }

        // Delegate to purpose-specific handler
        return handlers.stream()
                .filter(h -> h.supports(command.purpose()))
                .findFirst()
                .map(h -> h.handle(user))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "No handler found for purpose: " + command.purpose()));
    }

    private void processUserUpdates(User user, OtpToken.Purpose purpose) {
        switch (purpose) {
            case EMAIL_VERIFICATION -> {
                user.verifyEmail();
                userRepository.save(user);
                eventPublisher.publish(user.toEmailVerifiedEvent());
            }
            case PHONE_VERIFICATION -> {
                user.verifyPhone();
                userRepository.save(user);
            }
            case PASSWORD_RESET, LOGIN -> {
                // No user status update needed for PASSWORD_RESET and LOGIN
            }
        }
    }
}
