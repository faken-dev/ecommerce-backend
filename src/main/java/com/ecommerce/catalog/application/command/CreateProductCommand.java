package com.ecommerce.catalog.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductCommand(
        UUID sellerId,
        String name,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPerItem,
        String sku,
        String barcode,
        UUID categoryId,
        List<String> tags,
        String visibility,
        String metaTitle,
        String metaDescription,
        BigDecimal weightKg,
        String weightUnit
) {}