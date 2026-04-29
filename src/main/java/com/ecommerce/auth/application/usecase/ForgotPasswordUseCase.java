package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.ForgotPasswordCommand;
import com.ecommerce.auth.application.command.SendOtpCommand;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final SendOtpUseCase sendOtpUseCase;

    @Transactional
    public void execute(ForgotPasswordCommand command) {
        Email emailVo = new Email(command.email());

        // Load user Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â if user exists, send OTP for password reset.
        // If not, do nothing to prevent email enumeration.
        userRepository.findByEmail(emailVo).ifPresent(user -> {
            sendOtpUseCase.execute(new SendOtpCommand(
                    user.getEmail().value(),
                    OtpToken.Channel.EMAIL,
                    OtpToken.Purpose.PASSWORD_RESET
            ));
        });
    }
}
