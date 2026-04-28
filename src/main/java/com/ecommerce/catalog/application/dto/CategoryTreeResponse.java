package com.ecommerce.catalog.application.dto;

import java.util.List;
import java.util.UUID;

public record CategoryTreeResponse(
    UUID id,
    String slug,
    String name,
    String description,
    String iconUrl,
    int sortOrder,
    List<CategoryTreeResponse> children
) {}
