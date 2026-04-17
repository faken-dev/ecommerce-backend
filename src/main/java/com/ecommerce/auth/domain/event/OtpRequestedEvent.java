package com.ecommerce.auth.domain.event;

import com.ecommerce.auth.domain.entity.OtpToken;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when an OTP is requested (for login or password reset).
 * Triggers:
 * - OTP generation and storage
 * - Notification sending (email/SMS)
 *
 *  SECURITY: Event contains only metadata (userId, channel, purpose) — no raw OTP or token hashes.
 * Handlers must NOT log or expose sensitive info.
 */
public record OtpRequestedEvent(
        UUID userId,
        String destination,
        OtpToken.Channel channel,
        OtpToken.Purpose purpose,
        Instant occurredAt
) {}