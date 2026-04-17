package com.ecommerce.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published after a successful login (password or OTP).
 * Triggers:
 * - Post-login actions (e.g. logging, notifications)
 * - Security monitoring (e.g. detecting unusual login patterns)
 *
 * SECURITY: Event contains only userId and timestamp — no sensitive info.
 * Handlers must NOT log or expose sensitive info.
 */
public record LoginCompletedEvent(
        UUID userId,
        Instant occurredAt
) {}
