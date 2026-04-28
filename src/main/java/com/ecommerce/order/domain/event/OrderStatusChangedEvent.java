package com.ecommerce.order.domain.event;

import com.ecommerce.order.domain.entity.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        Instant occurredAt
) {}
