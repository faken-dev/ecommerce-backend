package com.ecommerce.auth.domain.event;

import com.ecommerce.auth.domain.entity.OtpToken;

import java.time.Instant;

/**
 * Event published when OTP needs to be sent for verification.
 * Triggers:
 * - SendOtpUseCase to generate & persist OTP
 * - OtpRequestedEvent (downstream) to actually send the notification
 */
public record OtpVerificationRequestedEvent(
        String email,
        OtpToken.Channel channel,
        OtpToken.Purpose purpose,
        Instant occurredAt
) {}
