package com.ecommerce.catalog.presentation.dto.request;

public record ProductImageRequest(
        String url,
        String altText,
        int sortOrder,
        boolean primary
) {}
