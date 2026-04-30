package com.ecommerce.user.application.handler;

import com.ecommerce.auth.domain.event.RegistrationCompletedEvent;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationHandler {

    private final UserProfileRepository userProfileRepository;

    @EventListener
    @Transactional
    public void handleRegistrationCompleted(RegistrationCompletedEvent event) {
        log.info("Handling RegistrationCompletedEvent for user: {}", event.userId());
        
        // Check if profile already exists (to avoid duplicate creation in case of event retry)
        if (userProfileRepository.findByUserId(event.userId()).isPresent()) {
            log.warn("User profile already exists for user: {}. Skipping creation.", event.userId());
            return;
        }

        UserProfile profile = UserProfile.createFromRegistration(
                event.userId(),
                event.fullName()
        );
        
        userProfileRepository.save(profile);
        log.info("Created initial profile for user: {}", event.userId());
    }
}
