package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserProfileUpdatedEvent(
        UUID userId,
        String fullName,
        String profilePictureUrl,
        Instant occurredAt
) {}
