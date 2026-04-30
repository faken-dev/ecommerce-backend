package com.ecommerce.catalog.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductCommand(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal compareAtPrice,
        UUID categoryId,
        List<String> tags,
        String metaTitle,
        String metaDescription,
        String threeDModelUrl,
        List<ProductImageCommand> images
) {}