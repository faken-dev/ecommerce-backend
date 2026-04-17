package com.ecommerce.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published after user registration is successfully persisted.
 * Triggers OTP verification flow (automatically sends OTP to verify email).
 */
public record RegistrationCompletedEvent(
        UUID userId,
        String email,
        String fullName,
        Instant occurredAt
) {}
