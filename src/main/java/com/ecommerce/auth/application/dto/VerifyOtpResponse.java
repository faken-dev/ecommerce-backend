package com.ecommerce.auth.application.dto;

import lombok.Builder;

@Builder
public record VerifyOtpResponse(
    UserResponse user,
    AuthTokenResponse tokens,
    String message
) {}
