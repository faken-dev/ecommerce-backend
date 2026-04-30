package com.ecommerce.order.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published when an order's payment is successfully confirmed.
 * This event triggers side effects like stock deduction and shipping initiation.
 */
public record OrderConfirmedEvent(
        UUID orderId,
        UUID buyerId,
        List<OrderItemData> items,
        Instant occurredAt
) {
    public record OrderItemData(
            UUID productId,
            UUID variantId,
            int quantity
    ) {}
}
