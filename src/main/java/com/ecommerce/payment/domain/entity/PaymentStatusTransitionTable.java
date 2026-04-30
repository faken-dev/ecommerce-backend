package com.ecommerce.payment.domain.entity;

import java.util.Set;

/**
 * Centralised transition table for {@link PaymentStatus} state machine.
 * All status transition rules live here - status enums delegate to this class.
 */
public enum PaymentStatusTransitionTable {

    ;

    private static final Set<PaymentStatus> TERMINAL = Set.of(
            PaymentStatus.REFUNDED,
            PaymentStatus.FAILED,
            PaymentStatus.CANCELLED
    );

    public static boolean canTransition(PaymentStatus from, PaymentStatus to) {
        if (from == to) return false;
        if (isTerminal(from)) return false;

        return switch (from) {
            case PENDING      -> to == PaymentStatus.PROCESSING
                                || to == PaymentStatus.FAILED
                                || to == PaymentStatus.CANCELLED;
            case PROCESSING   -> to == PaymentStatus.PAID || to == PaymentStatus.FAILED;
            case PAID         -> to == PaymentStatus.REFUNDING
                                || to == PaymentStatus.REFUNDED
                                || to == PaymentStatus.PARTIALLY_REFUNDED;
            case REFUNDING    -> to == PaymentStatus.REFUNDED
                                || to == PaymentStatus.PARTIALLY_REFUNDED
                                || to == PaymentStatus.REFUND_FAILED;
            case REFUND_FAILED -> to == PaymentStatus.REFUNDING;
            case PARTIALLY_REFUNDED -> to == PaymentStatus.REFUNDING;
            case FAILED, CANCELLED, REFUNDED -> false;
        };
    }

    public static boolean isTerminal(PaymentStatus status) {
        return TERMINAL.contains(status);
    }
}

