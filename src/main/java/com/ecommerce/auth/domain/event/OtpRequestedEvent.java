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
 *  SECURITY: Event contains metadata and the raw OTP code for the notification handler.
 * Handlers must NOT log the otpCode or expose it to public logs.
 */
public record OtpRequestedEvent(
        UUID userId,
        String destination,
        String otpCode,
        OtpToken.Channel channel,
        OtpToken.Purpose purpose,
        Instant occurredAt
) {}
