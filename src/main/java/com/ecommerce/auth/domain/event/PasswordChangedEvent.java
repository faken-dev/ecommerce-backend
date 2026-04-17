package com.ecommerce.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Published after a user successfully changes their password.
 * Triggers a security notification email to alert the user.
 */
public record PasswordChangedEvent(
        UUID userId,
        String email,
        String fullName,
        Instant occurredAt
) {}
