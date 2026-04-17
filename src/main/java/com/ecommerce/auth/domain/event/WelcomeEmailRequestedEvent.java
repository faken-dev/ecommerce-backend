package com.ecommerce.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published to trigger welcome email notification.
 * Sent only after email verification succeeds.
 */
public record WelcomeEmailRequestedEvent(
        UUID userId,
        String email,
        String fullName,
        Instant occurredAt
) {}
