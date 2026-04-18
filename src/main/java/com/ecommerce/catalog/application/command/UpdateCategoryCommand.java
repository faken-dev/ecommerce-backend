package com.ecommerce.catalog.application.command;

import java.util.UUID;

public record UpdateCategoryCommand(
        UUID id,
        String slug,
        String name,
        String description,
        String iconUrl,
        int sortOrder
) {}