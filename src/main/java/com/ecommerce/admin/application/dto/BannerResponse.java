package com.ecommerce.admin.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BannerResponse(
        UUID id,
        String imageUrl,
        String linkUrl,
        String title,
        String status,
        int priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
