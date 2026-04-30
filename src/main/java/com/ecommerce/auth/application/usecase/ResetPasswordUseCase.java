package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.ResetPasswordCommand;
import com.ecommerce.auth.application.command.VerifyOtpCommand;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    /**
     * Resets user password after OTP verification.
     * All existing refresh tokens are revoked to force re-login on all devices.
     */
    @Transactional
    public void execute(ResetPasswordCommand command) {
        var user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        verifyOtpUseCase.execute(new VerifyOtpCommand(
                command.email(), command.otpCode(), OtpToken.Purpose.PASSWORD_RESET), true);

        user.changePassword(HashedPassword.of(passwordEncoder.encode(command.newPassword())));
        userRepository.save(user);

        // Force re-login everywhere
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Publish security notification event
        eventPublisher.publish(user.toPasswordChangedEvent());
    }
}
