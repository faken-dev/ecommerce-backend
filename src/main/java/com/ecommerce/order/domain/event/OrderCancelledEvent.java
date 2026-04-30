package com.ecommerce.order.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID buyerId,
        UUID cancelledBy,
        List<OrderItemData> items,
        Instant occurredAt
) {
    public record OrderItemData(
            UUID productId,
            UUID variantId,
            int quantity
    ) {}
}
