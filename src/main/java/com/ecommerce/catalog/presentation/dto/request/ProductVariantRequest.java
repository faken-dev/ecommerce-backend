package com.ecommerce.catalog.presentation.dto.request;

import java.math.BigDecimal;

public record ProductVariantRequest(
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
