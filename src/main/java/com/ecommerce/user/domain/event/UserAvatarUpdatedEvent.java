package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a user updates their avatar (profile picture).
 */
public record UserAvatarUpdatedEvent(
    UUID userId,
    String newAvatarUrl,
    Instant occurredAt
) {
    public UserAvatarUpdatedEvent(UUID userId, String newAvatarUrl) {
        this(userId, newAvatarUrl, Instant.now());
    }
}
