package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String sku,
        String barcode,
        String title,
        BigDecimal price,
        BigDecimal compareAtPrice,
        int stockQuantity,
        String optionName,
        String optionValue,
        String option2Name,
        String option2Value,
        String imageUrl,
        boolean active
) {}
