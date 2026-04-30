package com.ecommerce.payment.infrastructure.scheduler;

import com.ecommerce.payment.application.service.PaymentReconciliationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background worker that triggers payment reconciliation periodically.
 */

@Component
@RequiredArgsConstructor
public class ReconciliationProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationProcessor.class);

    private final PaymentReconciliationService reconciliationService;

    /**
     * Runs periodically to check for stuck payments.
     */
    @Scheduled(fixedDelayString = "${payment.reconciliation.interval-ms:900000}")
    public void process() {
        log.debug("[ReconciliationProcessor] Starting reconciliation cycle...");
        try {
            reconciliationService.reconcileStuckPayments();
        } catch (Exception e) {
            log.error("[ReconciliationProcessor] Error during reconciliation cycle", e);
        }
    }
}
