package com.ecommerce.auth.application.dto;

public record RegisterResponse(
        UserResponse user,
        String message
) {}