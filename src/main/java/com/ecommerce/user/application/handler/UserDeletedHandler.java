package com.ecommerce.user.application.handler;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cascades user soft-deletion to UserProfile and Address rows.
 *
 * Listening to {@link User.DeletedEvent} (a domain record inside User.java).
 * Domain entities must not publish events directly — this handler is the
 * application-layer consumer that performs the actual cascade.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedHandler {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;

    @EventListener
    @Transactional
    public void handle(User.DeletedEvent event) {
        log.info("Cascading user deletion: userId={}", event.userId());

        userProfileRepository.deleteByUserId(event.userId());
        addressRepository.deleteAllByUserId(event.userId());

        log.info("Cascade deletion completed for userId={}", event.userId());
    }
}
