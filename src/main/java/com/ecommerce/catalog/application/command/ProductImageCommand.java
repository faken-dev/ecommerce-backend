package com.ecommerce.catalog.application.command;

public record ProductImageCommand(
    String url,
    String altText,
    int sortOrder,
    boolean primary
) {}
