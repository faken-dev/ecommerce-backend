package com.ecommerce.order.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddToCartCommand(
        UUID productId,
        UUID variantId,
        int quantity,
        BigDecimal unitPrice
) {}
