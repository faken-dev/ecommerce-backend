package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserProfileCreatedEvent(
        UUID profileId,
        UUID userId,
        String fullName,
        Instant occurredAt
) {}
