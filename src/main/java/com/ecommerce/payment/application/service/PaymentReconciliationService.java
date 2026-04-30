package com.ecommerce.payment.application.service;


import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.application.port.PaymentStatusChecker;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.shared.event.outbox.OutboxService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service to reconcile stuck payments with the external gateway status.
 */

@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayResolver gatewayResolver;
    private final OutboxService outboxService;
    private final PaymentProperties properties;
    private final MeterRegistry meterRegistry;
    private final TransactionTemplate transactionTemplate;

    /**
     * Polls for stuck payments and attempts to reconcile them.
     */
    public void reconcileStuckPayments() {
        // Find payments created more than X minutes ago and still in non-terminal state
        int thresholdMins = properties.getReconciliation().getThresholdMinutes();
        Instant threshold = Instant.now().minus(thresholdMins, ChronoUnit.MINUTES);
        List<Payment> stuckPayments = paymentRepository.findStuckPayments(threshold, PageRequest.of(0, 50));

        meterRegistry.gauge("payment.reconciliation.stuck.count", stuckPayments.size());

        if (stuckPayments.isEmpty()) {
            return;
        }

        log.info("[Reconciliation] Found {} stuck payments to process", stuckPayments.size());

        for (Payment payment : stuckPayments) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                transactionTemplate.executeWithoutResult(status -> reconcileSingle(payment));
                meterRegistry.counter("payment.reconciliation.success").increment();
            } catch (Exception e) {
                log.error("[Reconciliation] Failed to reconcile payment {}", payment.getId(), e);
                meterRegistry.counter("payment.reconciliation.error").increment();
            } finally {
                sample.stop(meterRegistry.timer("payment.reconciliation.duration"));
            }
        }
    }

    public void reconcileSingle(Payment payment) {
        log.info("[Reconciliation] Reconciling payment {} (current status: {})", 
                payment.getId(), payment.getStatus());

        PaymentStatusChecker checker = gatewayResolver.getStatusChecker(payment.getProvider());
        
        PaymentStatusChecker.ReconciliationResult result = checker.checkStatus(
                payment.getId(),
                payment.getOrderId().toString(),
                payment.getProvider(),
                payment.getProviderReference()
        );

        // Handle uninitiated stuck payments (missing reference)
        if ("MISSING_REFERENCE".equals(result.failureCode()) && payment.getStatus() == PaymentStatus.PENDING) {
            log.info("[Reconciliation] Marking uninitiated stuck payment {} as FAILED", payment.getId());
            payment.fail("ABANDONED_UNINITIATED", "Payment was never fully initiated or abandoned before redirection");
            Payment saved = paymentRepository.save(payment);
            outboxService.saveEvent(saved.toFailedEvent(), saved.getId().toString(), "Payment");
            return;
        }

        // Only update if status has changed or we received a new provider reference
        boolean changed = false;

        if (result.status() != payment.getStatus()) {
            log.info("[Reconciliation] Status change detected for {}: {} -> {}", 
                    payment.getId(), payment.getStatus(), result.status());
            
            if (result.status() == PaymentStatus.PAID) {
                payment.confirm(result.providerReference(), result.paidAt());
            } else if (result.status().isFailed()) {
                payment.fail(result.failureCode(), result.failureReason());
            }
            changed = true;
        }

        if (changed) {
            Payment saved = paymentRepository.save(payment);
            
            // Publish event via outbox if status changed to terminal
            if (saved.getStatus() == PaymentStatus.PAID) {
                outboxService.saveEvent(saved.toConfirmedEvent(), saved.getId().toString(), "Payment");
            } else if (saved.getStatus().isFailed()) {
                outboxService.saveEvent(saved.toFailedEvent(), saved.getId().toString(), "Payment");
            }
        }
    }
}
