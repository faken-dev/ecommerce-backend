package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.user.domain.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class UserProfileUpdatedHandler {
    private static final Logger log = LoggerFactory.getLogger(UserProfileUpdatedHandler.class);

    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void handle(UserProfileUpdatedEvent event) {
        log.info("Syncing profile data to Auth for userId={}", event.userId());
        userRepository.findById(event.userId())
                .ifPresent(user -> {
                    user.updateFullName(event.fullName());
                    user.updateProfilePictureUrl(event.profilePictureUrl());
                    userRepository.save(user);
                    log.info("Successfully synced profile data for userId={}", event.userId());
                });
    }
}
