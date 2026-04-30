package com.ecommerce.payment.domain.entity;

/**
 * Lifecycle of a single refund request.
 */
public enum RefundStatus {
    PENDING,    // Awaiting admin review
    APPROVED,   // Approved, forwarded to provider
    REJECTED,   // Rejected by admin
    COMPLETED,  // Provider confirmed refund
    FAILED;     // Provider failed to process refund

    public boolean canTransitionTo(RefundStatus target) {
        if (this == target) return false;
        return switch (this) {
            case PENDING   -> target == APPROVED || target == REJECTED;
            case APPROVED  -> target == COMPLETED || target == FAILED;
            case REJECTED, COMPLETED, FAILED -> false;
        };
    }
}
