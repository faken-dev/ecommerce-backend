package com.ecommerce.order.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Presentation-layer request to update a cart item quantity.
 * Wraps UpdateCartItemCommand for DDD compliance.
 */
public record UpdateCartItemRequest(
        @NotNull(message = "productId is required")
        UUID productId,

        UUID variantId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        int quantity
) {}
