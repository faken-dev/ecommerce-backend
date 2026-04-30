package com.ecommerce.catalog.application.dto;

import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String slug,
    String name,
    String description,
    UUID parentId,
    String parentName,
    String iconUrl,
    int sortOrder,
    boolean active
) {}
