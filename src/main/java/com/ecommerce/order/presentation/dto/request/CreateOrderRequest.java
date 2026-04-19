package com.ecommerce.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer request to create a new order from cart / direct checkout.
 * Wraps CreateOrderCommand for DDD compliance.
 */
public record CreateOrderRequest(
        @NotNull(message = "sellerId is required")
        UUID sellerId,

        @NotNull(message = "shippingAddressId is required")
        UUID shippingAddressId,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "subtotal is required")
        @Positive(message = "subtotal must be positive")
        BigDecimal subtotal,

        @NotNull(message = "shippingFee is required")
        BigDecimal shippingFee,

        @NotNull(message = "taxAmount is required")
        BigDecimal taxAmount,

        BigDecimal discountAmount,

        @NotNull(message = "currency is required")
        String currency,

        String buyerNote,

        String voucherCode
) {
    /**
     * Nested order item in the presentation request.
     */
    public record OrderItemRequest(
            @NotNull(message = "productId is required")
            UUID productId,

            UUID variantId,

            @NotNull(message = "quantity is required")
            @Positive(message = "quantity must be positive")
            int quantity,

            @NotNull(message = "unitPrice is required")
            @Positive(message = "unitPrice must be positive")
            BigDecimal unitPrice,

            String productName,
            String productSku,
            String productImageUrl,
            String variantTitle
    ) {}
}
