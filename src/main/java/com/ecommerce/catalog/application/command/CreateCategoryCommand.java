package com.ecommerce.catalog.application.command;

import java.util.UUID;

public record CreateCategoryCommand(
        String slug,
        String name,
        String description,
        UUID parentId
) {}
