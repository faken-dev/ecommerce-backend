package com.ecommerce.order.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Presentation-layer request to add an item to the cart.
 * Wraps AddToCartCommand for DDD compliance.
 */
public record AddToCartRequest(
        @NotNull(message = "productId is required")
        UUID productId,

        UUID variantId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        int quantity,

        @NotNull(message = "unitPrice is required")
        @Positive(message = "unitPrice must be positive")
        BigDecimal unitPrice
) {}
