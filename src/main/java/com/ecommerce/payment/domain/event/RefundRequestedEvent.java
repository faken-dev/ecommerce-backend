package com.ecommerce.payment.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundRequestedEvent(
        UUID refundId,
        UUID paymentId,
        UUID orderId,
        UUID requestedBy,
        BigDecimal amount,
        String reason,
        Instant occurredAt
) {}
