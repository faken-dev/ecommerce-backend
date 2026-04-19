package com.ecommerce.catalog.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Presentation-layer request to update a category.
 * Category ID is taken from the path, not this request.
 */
public record UpdateCategoryRequest(
        @NotBlank(message = "Slug is required")
        String slug,

        @NotBlank(message = "Name is required")
        String name,

        String description,
        String iconUrl,
        int sortOrder
) {}