package com.ecommerce.auth.application.command;

public record LoginCommand(
        String email,
        String password,
        String deviceInfo,
        String ipAddress,
        /** Required when captcha challenge is presented. Null otherwise. */
        String captchaToken
) {}
