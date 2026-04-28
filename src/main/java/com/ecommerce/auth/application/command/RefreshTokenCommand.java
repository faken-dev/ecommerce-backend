package com.ecommerce.auth.application.command;

public record RefreshTokenCommand(
        String refreshToken,
        String deviceInfo,
        String ipAddress
) {}
