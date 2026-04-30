package com.ecommerce.payment.domain.entity;

import com.ecommerce.payment.domain.event.PaymentCancelledEvent;
import com.ecommerce.payment.domain.event.PaymentConfirmedEvent;
import com.ecommerce.payment.domain.event.PaymentFailedEvent;
import com.ecommerce.payment.domain.event.RefundRequestedEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Payment extends AuditableEntity {

    private UUID orderId;
    private UUID buyerId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String currency;
    private PaymentProvider provider;
    private PaymentMethodType methodType;
    private String providerReference;
    private PaymentStatus status;
    private String failureReason;
    private String failureCode;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private Instant paidAt;
    private Instant cancelledAt;
    private Instant expiredAt;
    private Instant deletedAt;
    private String ipAddress;
    private String userAgent;
    private String idempotencyKey;
    private Long version;
    @Builder.Default
    private List<Refund> refunds = new ArrayList<>();

    public static Payment create(
            UUID orderId,
            UUID buyerId,
            BigDecimal amount,
            String currency,
            PaymentProvider provider,
            PaymentMethodType methodType,
            String description,
            String idempotencyKey,
            String returnUrl,
            String cancelUrl,
            String ipAddress,
            String userAgent) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Payment amount must be positive");
        }

        return Payment.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .orderId(orderId)
                .buyerId(buyerId)
                .amount(amount)
                .refundedAmount(BigDecimal.ZERO)
                .currency(currency != null ? currency : "VND")
                .provider(provider)
                .methodType(methodType)
                .status(PaymentStatus.PENDING)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    public void initiate(String providerReference) {
        validateTransition(PaymentStatus.PROCESSING);
        if (providerReference == null || providerReference.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Provider reference is required to initiate a payment");
        }
        this.providerReference = providerReference;
        this.status = PaymentStatus.PROCESSING;
        this.touchUpdate();
    }

    public void confirm(String providerReference, Instant paidAt) {
        validateTransition(PaymentStatus.PAID);
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt != null ? paidAt : Instant.now();
        if (providerReference != null && !providerReference.isBlank()) {
            this.providerReference = providerReference;
        }
        this.touchUpdate();
    }

    public void fail(String failureCode, String failureReason) {
        validateTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.touchUpdate();
    }

    public void cancel(UUID cancelledBy) {
        validateTransition(PaymentStatus.CANCELLED);
        if (!this.buyerId.equals(cancelledBy)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Only the payment owner can cancel");
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.touchUpdate();
    }

    public RefundRequestResult requestRefund(
            UUID orderId,
            UUID requestedBy,
            BigDecimal amount,
            String reason,
            String ipAddress) {

        if (!status.isRefundable()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_REFUNDABLE,
                    "Payment in status %s cannot be refunded".formatted(status));
        }

        BigDecimal maxRefundable = getMaxRefundableAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.compareTo(maxRefundable) > 0) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID,
                    "Refund amount must be between 0 and %s (max refundable)".formatted(maxRefundable));
        }

        validateTransition(PaymentStatus.REFUNDING);
        this.status = PaymentStatus.REFUNDING;
        this.touchUpdate();

        Refund refund = Refund.create(
                UuidCreator.getTimeOrderedEpoch(),
                getId(),
                orderId,
                requestedBy,
                amount,
                reason,
                ipAddress);

        this.refunds.add(refund);
        return new RefundRequestResult(refund, refund.toRequestedEvent());
    }

    public void approveRefund(UUID refundId, String providerRefundId, Instant approvedAt) {
        Refund refund = findRefundOrThrow(refundId);
        refund.approve(providerRefundId, approvedAt);

        this.refundedAmount = refunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.APPROVED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else if (this.refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        this.touchUpdate();
    }

    public void rejectRefund(UUID refundId, String reason) {
        Refund refund = findRefundOrThrow(refundId);
        refund.reject(reason);
        if (this.refundedAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = PaymentStatus.PAID;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        this.touchUpdate();
    }

    private Refund findRefundOrThrow(UUID refundId) {
        return refunds.stream()
                .filter(r -> r.getId().equals(refundId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PAYMENT_REFUND_NOT_FOUND,
                        "Refund %s not found in payment %s".formatted(refundId, getId())));
    }

    public BigDecimal getMaxRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    public boolean isFullyRefunded() {
        return refundedAmount.compareTo(amount) >= 0;
    }

    public boolean isOwnedBy(UUID userId) {
        return this.buyerId.equals(userId);
    }

    public void addRefund(Refund refund) {
        this.refunds.add(refund);
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.touchUpdate();
    }

    public PaymentConfirmedEvent toConfirmedEvent() {
        return new PaymentConfirmedEvent(
                getId(), orderId, buyerId, amount, currency,
                provider, providerReference, paidAt);
    }

    public PaymentFailedEvent toFailedEvent() {
        return new PaymentFailedEvent(
                getId(), orderId, buyerId, failureCode, failureReason);
    }

    public PaymentCancelledEvent toCancelledEvent(UUID cancelledBy) {
        return new PaymentCancelledEvent(
                getId(), orderId, buyerId, cancelledBy);
    }

    public void updateStatus(PaymentStatus newStatus) {
        validateTransition(newStatus);
        this.status = newStatus;
        if (newStatus == PaymentStatus.PAID) {
            this.paidAt = Instant.now();
        } else if (newStatus == PaymentStatus.CANCELLED) {
            this.cancelledAt = Instant.now();
        }
        this.touchUpdate();
    }

    private void validateTransition(PaymentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_STATUS_TRANSITION,
                    "Cannot transition payment from %s to %s".formatted(status, target));
        }
    }

    public record RefundRequestResult(
            Refund refund,
            RefundRequestedEvent event
    ) {}
}
