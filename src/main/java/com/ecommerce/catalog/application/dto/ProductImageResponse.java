package com.ecommerce.catalog.application.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        String altText,
        int sortOrder,
        boolean primary
) {}
