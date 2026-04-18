package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
    UUID id,
    String name,
    String slug,
    BigDecimal price,
    BigDecimal compareAtPrice,
    int stockQuantity,
    String status,
    boolean isFeatured,
    BigDecimal averageRating,
    int reviewCount
) {}