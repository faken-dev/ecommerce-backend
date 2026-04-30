package com.ecommerce.order.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        String voucherCode,
        List<OrderItemData> items,
        Set<UUID> categoryIds,
        Instant occurredAt
) {
    public record OrderItemData(
            UUID productId,
            UUID variantId,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
