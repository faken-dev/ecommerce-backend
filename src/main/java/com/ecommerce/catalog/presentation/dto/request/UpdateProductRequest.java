package com.ecommerce.catalog.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 300, message = "Name must be at most 300 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 350, message = "Slug must be at most 350 characters")
        String slug,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0", message = "Price cannot be negative")
        BigDecimal price,

        BigDecimal compareAtPrice,
        UUID categoryId,
        List<String> tags,
        String metaTitle,
        String metaDescription,
        String threeDModelUrl,
        List<ProductImageRequest> images
) {}
