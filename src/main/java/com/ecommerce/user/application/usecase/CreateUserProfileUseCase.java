package com.ecommerce.user.application.usecase;

import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(UUID userId, String fullName) {
        if (userProfileRepository.existsByUserId(userId)) {
            return;
        }

        UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName(fullName)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();

        UserProfile saved = userProfileRepository.save(profile);
        eventPublisher.publish(saved.toCreatedEvent());
    }
}
