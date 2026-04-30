package com.ecommerce.payment.domain.entity;

/**
 * Payment aggregate status - reflects the lifecycle of a payment attempt.
 *
 * Valid transitions:
 * PENDING    → PROCESSING, FAILED, CANCELLED
 * PROCESSING → PAID, FAILED
 * PAID       → REFUNDING, REFUNDED, PARTIALLY_REFUNDED
 * REFUNDING  → REFUNDED, PARTIALLY_REFUNDED, REFUND_FAILED
 * REFUND_FAILED → REFUNDING   (can retry)
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    REFUNDING,
    REFUNDED,
    PARTIALLY_REFUNDED,
    REFUND_FAILED,
    CANCELLED;

    public boolean canTransitionTo(PaymentStatus target) {
        return PaymentStatusTransitionTable.canTransition(this, target);
    }

    public boolean isTerminal() {
        return this == REFUNDED || this == CANCELLED || this == FAILED;
    }

    public boolean isFailed() {
        return this == FAILED || this == REFUND_FAILED;
    }

    public boolean isRefundable() {
        return this == PAID || this == PARTIALLY_REFUNDED;
    }
}
