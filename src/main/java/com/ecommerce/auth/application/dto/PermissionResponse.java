package com.ecommerce.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
