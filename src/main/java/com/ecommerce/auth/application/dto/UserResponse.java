package com.ecommerce.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phoneNumber,
        boolean emailVerified,
        boolean phoneVerified,
        Set<String> roles
) {}