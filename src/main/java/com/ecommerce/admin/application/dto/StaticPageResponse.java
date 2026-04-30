package com.ecommerce.admin.application.dto;

import java.time.Instant;
import java.util.UUID;

public record StaticPageResponse(
    UUID id,
    String title,
    String slug,
    String content,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
