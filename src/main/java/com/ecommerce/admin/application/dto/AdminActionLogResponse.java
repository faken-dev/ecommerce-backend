package com.ecommerce.admin.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminActionLogResponse(
        UUID id,
        UUID adminId,
        String adminEmail,
        String action,
        String targetType,
        String targetId,
        String details,
        String ipAddress,
        OffsetDateTime createdAt
) {}
