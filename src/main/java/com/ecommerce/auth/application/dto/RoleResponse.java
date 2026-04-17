package com.ecommerce.auth.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        List<PermissionResponse> permissions,
        Instant createdAt,
        Instant updatedAt
) {}
