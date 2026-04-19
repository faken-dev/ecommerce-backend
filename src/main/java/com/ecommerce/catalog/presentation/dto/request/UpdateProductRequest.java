package com.ecommerce.catalog.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer request to update a product.
 * Product ID is taken from the path, not this request.
 */
public record UpdateProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        @NotBlank(message = "Slug is required")
        String slug,

        String description,

        @DecimalMin(value = "0", message = "Price cannot be negative")
        BigDecimal price,

        BigDecimal compareAtPrice,
        UUID categoryId,
        List<String> tags,
        String metaTitle,
        String metaDescription
) {}