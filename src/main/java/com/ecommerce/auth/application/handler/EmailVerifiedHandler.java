package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.domain.event.EmailVerifiedEvent;
import com.ecommerce.auth.domain.event.LoginCompletedEvent;
import com.ecommerce.auth.domain.event.WelcomeEmailRequestedEvent;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Async handler for EmailVerifiedEvent.
 *
 * Runs after transaction commit. Does NOT generate tokens - that is done synchronously
 * by AuthTokenOtpHandler within the same transaction so the client receives tokens
 * immediately after OTP verification.
 */

@Component
@RequiredArgsConstructor
public class EmailVerifiedHandler {
    private static final Logger log = LoggerFactory.getLogger(EmailVerifiedHandler.class);

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @EventListener
    public void handle(EmailVerifiedEvent event) {
        var userOpt = userRepository.findById(event.userId());
        if (userOpt.isEmpty()) {
            log.error("User not found for EmailVerifiedEvent: userId={}", event.userId());
            return;
        }

        eventPublisher.publish(new LoginCompletedEvent(event.userId(), event.occurredAt()));
        eventPublisher.publish(new WelcomeEmailRequestedEvent(
                event.userId(), event.email(), event.fullName(), event.occurredAt()));

        log.info("EmailVerifiedEvent handled for userId={}", event.userId());
    }
}