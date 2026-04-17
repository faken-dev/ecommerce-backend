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

import java.util.List;

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
        // Load user
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        // Find active OTP
        OtpToken token = otpTokenRepository
                .findLatestActiveByUserIdAndPurpose(user.getId(), command.purpose())
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        // Validate: expiry + usage + attempt count. Throws on failure.
        token.validateForVerification(maxAttempts);

        // Verify code: HMAC-SHA256 hash comparison
        if (!otpHasher.verify(command.code(), token.getCodeHash())) {
            // token.validateForVerification() already incremented attemptCount above;
            // just persist the update (single save — no double-increment).
            otpTokenRepository.save(token);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        token.markAsUsed();
        otpTokenRepository.save(token);

        processUserUpdates(user, command.purpose());

        // Delegate to purpose-specific handler. Returns tokens for LOGIN and EMAIL_VERIFICATION.
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
