package com.ecommerce.order.domain.entity;

/**
 * Order status state machine.
 *
 * Valid transitions are defined centrally in {@link OrderStatusTransitionTable}.
 *
 * Valid transitions:
 * PENDING      → CONFIRMED, CANCELLED
 * CONFIRMED    → PROCESSING, CANCELLED (if within cancel window)
 * PROCESSING   → SHIPPED, CANCELLED (by admin only)
 * SHIPPED      → DELIVERED
 * DELIVERED    → (REFUNDED via separate refund flow)
 * CANCELLED    → (terminal)
 * REFUNDED     → PARTIALLY_REFUNDED (terminal)
 * PARTIALLY_REFUNDED → (terminal)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED;

    public boolean canTransitionTo(OrderStatus target) {
        return OrderStatusTransitionTable.canTransition(this, target);
    }

    public boolean isTerminal() {
        return OrderStatusTransitionTable.isTerminal(this);
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}