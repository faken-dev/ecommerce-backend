package com.ecommerce.order.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        BigDecimal totalAmount,
        Instant occurredAt
) {}