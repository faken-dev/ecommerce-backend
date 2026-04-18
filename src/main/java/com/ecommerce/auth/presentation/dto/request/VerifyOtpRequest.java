package com.ecommerce.auth.presentation.dto.request;

import com.ecommerce.auth.domain.entity.OtpToken;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "OTP code is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String code,

        @NotNull(message = "Purpose is required")
        OtpToken.Purpose purpose
) {}