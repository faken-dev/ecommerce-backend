package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    UUID sellerId,
    String name,
    String slug,
    String description,
    BigDecimal price,
    BigDecimal compareAtPrice,
    BigDecimal costPerItem,
    int stockQuantity,
    int lowStockThreshold,
    String sku,
    String barcode,
    UUID categoryId,
    List<String> tags,
    String status,
    boolean isFeatured,
    String visibility,
    String metaTitle,
    String metaDescription,
    BigDecimal averageRating,
    int reviewCount,
    BigDecimal weightKg,
    String weightUnit,
    Instant createdAt,
    Instant updatedAt
) {}