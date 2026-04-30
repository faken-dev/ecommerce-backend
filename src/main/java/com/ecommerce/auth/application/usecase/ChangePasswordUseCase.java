package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.ChangePasswordCommand;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(ChangePasswordCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Verify current password
        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash().value())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Update password
        user.changePassword(HashedPassword.of(passwordEncoder.encode(command.newPassword())));
        userRepository.save(user);

        // Revoke all other sessions (optional, but safer)
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Publish event
        eventPublisher.publish(user.toPasswordChangedEvent());
    }
}
