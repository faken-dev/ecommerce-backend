package com.ecommerce.order.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID buyerId,
        UUID cancelledBy,
        Instant occurredAt
) {}