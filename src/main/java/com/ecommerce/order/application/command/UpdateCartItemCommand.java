package com.ecommerce.order.application.command;

import java.util.UUID;

public record UpdateCartItemCommand(
        UUID productId,
        UUID variantId,
        int quantity
) {}
