package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.application.command.SendOtpCommand;
import com.ecommerce.auth.application.usecase.SendOtpUseCase;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.event.OtpVerificationRequestedEvent;
import com.ecommerce.auth.domain.event.RegistrationCompletedEvent;
import com.ecommerce.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OtpVerificationRequestedHandler {
    private static final Logger log = LoggerFactory.getLogger(OtpVerificationRequestedHandler.class);

    private final SendOtpUseCase sendOtpUseCase;

    @EventListener
    public void handle(RegistrationCompletedEvent event) {
        log.info("Registration completed for userId={} Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â sending OTP verification",
                event.userId());

        try {
            sendOtpUseCase.execute(new SendOtpCommand(
                    event.email(),
                    OtpToken.Channel.EMAIL,
                    OtpToken.Purpose.EMAIL_VERIFICATION
            ));
        } catch (BusinessException e) {
            // OTP send failed Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â log nhÄ‚â€ Ă‚Â°ng khĂ„â€Ă‚Â´ng rollback transaction register
            log.error("Failed to send OTP after registration for userId={}: {}",
                    event.userId(), e.getMessage());
        }
    }

    @EventListener
    public void handle(OtpVerificationRequestedEvent event) {
        log.info("OTP verification requested for userId={}, purpose={}",
                event.email(), event.purpose());

        try {
            sendOtpUseCase.execute(new SendOtpCommand(
                    event.email(),
                    event.channel(),
                    event.purpose()
            ));
        } catch (BusinessException e) {
            log.error("Failed to send OTP for userId={}, purpose={}: {}",
                    event.email(), event.purpose(), e.getMessage());
        }
    }
}
