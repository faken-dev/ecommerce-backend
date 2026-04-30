package com.ecommerce.payment.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundCompletedEvent(
        UUID refundId,
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        Instant completedAt
) {}
