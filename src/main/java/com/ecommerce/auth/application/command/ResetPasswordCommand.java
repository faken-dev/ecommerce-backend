package com.ecommerce.auth.application.command;

public record ResetPasswordCommand(
        String email,
        String otpCode,
        String newPassword
) {}
