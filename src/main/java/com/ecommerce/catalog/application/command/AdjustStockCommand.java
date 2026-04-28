package com.ecommerce.catalog.application.command;

import java.util.UUID;

public record AdjustStockCommand(UUID productId, int adjustment) {}
