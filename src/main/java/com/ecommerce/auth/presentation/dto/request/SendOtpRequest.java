package com.ecommerce.auth.presentation.dto.request;

import com.ecommerce.auth.domain.entity.OtpToken;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendOtpRequest(
        @NotBlank(message = "Email is required")
        String email,

        @NotNull(message = "Channel is required")
        OtpToken.Channel channel,

        @NotNull(message = "Purpose is required")
        OtpToken.Purpose purpose
) {}
