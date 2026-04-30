package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.user.domain.event.UserAvatarUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for avatar updates from the User module and synchronizes them
 * with the Auth module's User record.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAvatarSyncHandler {

    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void handle(UserAvatarUpdatedEvent event) {
        log.debug("Syncing avatar for user {}: {}", event.userId(), event.newAvatarUrl());
        userRepository.updateAvatar(event.userId(), event.newAvatarUrl());
    }
}
