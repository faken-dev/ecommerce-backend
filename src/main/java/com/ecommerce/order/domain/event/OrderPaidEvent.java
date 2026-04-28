package com.ecommerce.order.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        BigDecimal amount,
        Instant occurredAt
) {}
