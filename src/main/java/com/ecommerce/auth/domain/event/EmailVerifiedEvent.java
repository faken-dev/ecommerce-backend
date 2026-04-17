package com.ecommerce.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published after email verification succeeds.
 * Triggers:
 * - Token generation (login flow)
 * - Welcome email notification
 */
public record EmailVerifiedEvent(
        UUID userId,
        String email,
        String fullName,
        Instant occurredAt
) {}
