package com.ecommerce.payment.application.port;

import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Port for querying the real-time status of a payment from an external gateway.
 */
public interface PaymentStatusChecker {

    /**
     * Queries the gateway for the current state of the transaction.
     */
    ReconciliationResult checkStatus(
            UUID paymentId,
            String orderId,
            PaymentProvider provider,
            String providerReference
    );

    record ReconciliationResult(
            PaymentStatus status,
            String providerReference,
            Instant paidAt,
            String failureCode,
            String failureReason
    ) {}
}
