package com.ecommerce.auth.application.command;


import com.ecommerce.auth.domain.entity.OtpToken;

public record VerifyOtpCommand(
        String email,
        String code,
        OtpToken.Purpose purpose
) {}
