package com.ecommerce.payment.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundApprovedEvent(
        UUID refundId,
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String providerRefundId,
        Instant approvedAt
) {}
