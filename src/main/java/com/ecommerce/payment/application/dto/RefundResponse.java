package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.entity.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
    UUID id,
    UUID paymentId,
    UUID orderId,
    UUID requestedBy,
    BigDecimal amount,
    String reason,
    RefundStatus status,
    String rejectionReason,
    String providerRefundId,
    Instant approvedAt,
    Instant rejectedAt,
    Instant completedAt,
    Instant failedAt,
    Instant createdAt
) {}
