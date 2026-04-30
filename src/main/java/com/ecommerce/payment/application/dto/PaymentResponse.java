package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.entity.Refund;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    UUID orderId,
    UUID buyerId,
    BigDecimal amount,
    BigDecimal refundedAmount,
    BigDecimal maxRefundableAmount,
    String currency,
    PaymentProvider provider,
    PaymentMethodType methodType,
    String providerReference,
    PaymentStatus status,
    String failureReason,
    String failureCode,
    String description,
    String returnUrl,
    String cancelUrl,
    Instant paidAt,
    Instant cancelledAt,
    Instant createdAt,
    List<RefundResponse> refunds
) {

    public record RefundResponse(
        UUID id,
        UUID paymentId,
        UUID orderId,
        UUID requestedBy,
        BigDecimal amount,
        String reason,
        String status,
        String rejectionReason,
        String providerRefundId,
        Instant approvedAt,
        Instant rejectedAt,
        Instant completedAt,
        Instant failedAt,
        Instant createdAt
    ) {
        public static RefundResponse from(Refund refund) {
            return new RefundResponse(
                refund.getId(),
                refund.getPaymentId(),
                refund.getOrderId(),
                refund.getRequestedBy(),
                refund.getAmount(),
                refund.getReason(),
                refund.getStatus().name(),
                refund.getRejectionReason(),
                refund.getProviderRefundId(),
                refund.getApprovedAt(),
                refund.getRejectedAt(),
                refund.getCompletedAt(),
                refund.getFailedAt(),
                refund.getCreatedAt()
            );
        }
    }
}
