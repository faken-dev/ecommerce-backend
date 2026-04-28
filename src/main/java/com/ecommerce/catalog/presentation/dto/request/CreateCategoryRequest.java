package com.ecommerce.catalog.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Presentation-layer request to create a new category.
 */
public record CreateCategoryRequest(
        @NotBlank(message = "Slug is required")
        @Size(max = 100, message = "Slug must be at most 100 characters")
        String slug,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        String description,

        UUID parentId
) {}
