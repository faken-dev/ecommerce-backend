package com.ecommerce.user.application.handler;

import com.ecommerce.auth.domain.event.RegistrationCompletedEvent;
import com.ecommerce.user.application.usecase.CreateUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to RegistrationCompletedEvent and creates a UserProfile row.
 * If profile creation fails, the exception propagates and causes the
 * entire transaction to roll back - user registration and profile creation
 * are atomic.
 */

@Component
@RequiredArgsConstructor
public class UserProfileCreatedHandler {
    private static final Logger log = LoggerFactory.getLogger(UserProfileCreatedHandler.class);

    private final CreateUserProfileUseCase createUserProfileUseCase;

    @EventListener
    public void handle(RegistrationCompletedEvent event) {
        log.info("Creating user profile after registration for userId={}", event.userId());
        createUserProfileUseCase.execute(event.userId(), event.fullName());
        log.info("User profile created successfully for userId={}", event.userId());
    }
}
