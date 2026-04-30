package com.ecommerce.catalog.application.command;

import java.util.UUID;

public record SetStockCommand(
    UUID productId,
    int quantity
) {}
