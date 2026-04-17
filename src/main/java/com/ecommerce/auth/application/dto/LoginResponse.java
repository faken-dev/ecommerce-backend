package com.ecommerce.auth.application.dto;

public record LoginResponse(
        UserResponse user,
        AuthTokenResponse tokens
) {}