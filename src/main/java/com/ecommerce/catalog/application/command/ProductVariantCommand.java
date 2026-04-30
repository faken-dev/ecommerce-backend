package com.ecommerce.catalog.application.command;

import java.math.BigDecimal;


public record ProductVariantCommand(
        String sku,
        String barcode,
        String title,
        BigDecimal price,
        BigDecimal compareAtPrice,
        String optionName,
        String optionValue,
        String option2Name,
        String option2Value,
        String imageUrl,
        boolean active
) {}
