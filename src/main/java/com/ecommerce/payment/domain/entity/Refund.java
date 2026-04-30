package com.ecommerce.payment.domain.entity;

import com.ecommerce.payment.domain.event.RefundApprovedEvent;
import com.ecommerce.payment.domain.event.RefundRejectedEvent;
import com.ecommerce.payment.domain.event.RefundRequestedEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Refund extends AuditableEntity {

    private UUID paymentId;
    private UUID orderId;
    private UUID requestedBy;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String rejectionReason;
    private String providerRefundId;
    private String ipAddress;
    private Instant approvedAt;
    private Instant rejectedAt;
    private Instant completedAt;
    private Instant failedAt;
    private Instant deletedAt;

    public static Refund create(
            UUID id,
            UUID paymentId,
            UUID orderId,
            UUID requestedBy,
            BigDecimal amount,
            String reason,
            String ipAddress) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID,
                    "Refund amount must be positive");
        }

        return Refund.builder()
                .id(id)
                .paymentId(paymentId)
                .orderId(orderId)
                .requestedBy(requestedBy)
                .amount(amount)
                .reason(reason)
                .status(RefundStatus.PENDING)
                .ipAddress(ipAddress)
                .build();
    }

    public void approve(String providerRefundId, Instant approvedAt) {
        if (this.status != RefundStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION,
                    "Refund can only be approved from PENDING status");
        }
        this.status = RefundStatus.APPROVED;
        this.providerRefundId = providerRefundId;
        this.approvedAt = approvedAt != null ? approvedAt : Instant.now();
        this.touchUpdate();
    }

    public void reject(String reason) {
        if (this.status != RefundStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION,
                    "Refund can only be rejected from PENDING status");
        }
        this.status = RefundStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectedAt = Instant.now();
        this.touchUpdate();
    }

    public void complete() {
        if (this.status != RefundStatus.APPROVED) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION,
                    "Refund can only be completed from APPROVED status");
        }
        this.status = RefundStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.touchUpdate();
    }

    public void fail() {
        if (this.status != RefundStatus.APPROVED) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION,
                    "Refund can only be failed from APPROVED status");
        }
        this.status = RefundStatus.FAILED;
        this.failedAt = Instant.now();
        this.touchUpdate();
    }

    public RefundRequestedEvent toRequestedEvent() {
        return new RefundRequestedEvent(
                getId(), paymentId, orderId, requestedBy,
                amount, reason, Instant.now());
    }

    public RefundApprovedEvent toApprovedEvent() {
        return new RefundApprovedEvent(
                getId(), paymentId, orderId, amount, providerRefundId, approvedAt);
    }

    public RefundRejectedEvent toRejectedEvent() {
        return new RefundRejectedEvent(
                getId(), paymentId, orderId, amount, rejectionReason, rejectedAt);
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.touchUpdate();
    }
}
