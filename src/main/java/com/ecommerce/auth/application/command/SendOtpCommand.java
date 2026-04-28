package com.ecommerce.auth.application.command;


import com.ecommerce.auth.domain.entity.OtpToken;

public record SendOtpCommand(
        String email,
        OtpToken.Channel channel,
        OtpToken.Purpose purpose
) {}
