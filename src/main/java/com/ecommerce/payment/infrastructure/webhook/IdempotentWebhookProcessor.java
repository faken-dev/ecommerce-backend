package com.ecommerce.payment.infrastructure.webhook;

import java.util.Optional;

import com.ecommerce.payment.application.port.PaymentWebhookHandler.WebhookResult;
import com.ecommerce.payment.application.usecase.ConfirmPaymentUseCase;
import com.ecommerce.payment.application.usecase.FailPaymentUseCase;
import com.ecommerce.payment.application.command.ConfirmPaymentCommand;
import com.ecommerce.payment.application.command.FailPaymentCommand;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.application.port.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Ensures webhook events are processed at most once (idempotent).
 *
 * <p>Problem: Payment gateways send webhooks via at-least-once delivery.
 * The same event (e.g., payment_intent.succeeded) can arrive multiple times.
 * Without idempotency, we risk confirming the same payment twice,
 * triggering double order fulfillment.
 *
 * <p>Solution: Redis deduplication using event fingerprint:
 * <pre>
 * key = "webhook:processed:{provider}:{providerReference}"
 * value = timestamp + event type
 * TTL = 24 hours
 * </pre>
 *
 * <p>Flow:
 * <ol>
 *   <li>Try SET NX on Redis key (atomic)</li>
 *   <li>If key already exists → skip (already processed)</li>
 *   <li>If new key → process event, then set key with TTL</li>
 * </ol>
 *
 * <p>If Redis is unavailable, falls back to DB optimistic locking
 * (check payment status before confirming).
 *
 * <p>Thread-safety: Uses Redis SETNX (atomic) - no race conditions.
 */

@Component
@RequiredArgsConstructor
public class IdempotentWebhookProcessor {
    private static final Logger log = LoggerFactory.getLogger(IdempotentWebhookProcessor.class);

    private static final String KEY_PREFIX = "webhook:processed:";
    private static final Duration DEDUP_TTL = Duration.ofHours(24);

    private final PaymentRepository paymentRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final FailPaymentUseCase failPaymentUseCase;
    private final RedisTemplate<String, String> redisTemplate;
    private final DistributedLockPort lockPort;

    /**
     * Processes a webhook event idempotently.
     * Same event arriving twice → second call is no-op.
     *
     * @param provider       Payment provider
     * @param result         Parsed webhook result
     * @param rawPayload     Raw payload (for logging/audit)
     */
    public void process(PaymentProvider provider, WebhookResult result, String rawPayload) {

        String dedupKey = buildDedupKey(provider, result);

        // - Step 1: Distributed Lock & Idempotency check -----------
        lockPort.executeWithLock(dedupKey, Duration.ofSeconds(10), Duration.ofMinutes(1), () -> {
            
            // Check if already processed (Done status in Redis)
            String status = redisTemplate.opsForValue().get(dedupKey);
            if (status != null && status.startsWith("DONE")) {
                log.info("[Webhook/Idempotent] Event already processed [key={}]", dedupKey);
                return;
            }

            // - Step 2: Process the event -
            try {
                doProcess(provider, result);

                // Mark as successfully processed with TTL
                redisTemplate.opsForValue().set(dedupKey, "DONE:" + result.status(), DEDUP_TTL);
                log.debug("[Webhook/Idempotent] Marked event as processed [key={}]", dedupKey);

            } catch (Exception e) {
                log.error("[Webhook/Idempotent] Processing failed [key={}]", dedupKey, e);
                throw e;
            }
        });
    }

    private void doProcess(PaymentProvider provider, WebhookResult result) {
        log.info("[Webhook] Processing event [provider={}, ref={}, status={}]",
                provider, result.providerReference(), result.status());

        // Find payment: Try txnRef first (our internal ID), then providerReference
        Payment payment = null;
        if (result.txnRef() != null) {
            payment = findByPaymentIdFallback(result.txnRef()).orElse(null);
        }

        if (payment == null) {
            payment = paymentRepository.findByProviderReference(result.providerReference())
                    .orElseGet(() -> findByPaymentIdFallback(result.providerReference()).orElse(null));
        }

        if (payment == null) {
            log.warn("[Webhook] Payment not found for reference [{}]", result.providerReference());
            return;
        }

        PaymentStatus newStatus = result.status();
        PaymentStatus currentStatus = payment.getStatus();

        // - Idempotency via DB: skip if already at target status -------
        if (newStatus == currentStatus || (newStatus == PaymentStatus.PAID && currentStatus.isTerminal())) {
            log.info("[Webhook] Payment {} already at status {} - skipping", payment.getId(), currentStatus);
            return;
        }

        switch (newStatus) {
            case PAID -> confirmPaymentUseCase.execute(new ConfirmPaymentCommand(
                    payment.getId(),
                    result.providerReference(),
                    result.paidAt()
            ));
            case FAILED, CANCELLED -> failPaymentUseCase.execute(new FailPaymentCommand(
                    payment.getId(),
                    result.failureCode(),
                    result.failureReason()
            ));
            default -> log.info("[Webhook] Unhandled webhook status [{}] for payment [{}]",
                    newStatus, payment.getId());
        }
    }

    /**
     * Fallback: tries to find payment by treating the reference as our internal payment ID.
     * Useful for redirect-based providers (VNPay, MoMo) where providerReference = paymentId.
     */
    private Optional<Payment> findByPaymentIdFallback(String reference) {
        try {
            UUID paymentId = UUID.fromString(reference);
            return paymentRepository.findById(paymentId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String buildDedupKey(PaymentProvider provider, WebhookResult result) {
        String eventId = result.webhookEventId() != null ? result.webhookEventId() : result.providerReference();
        return KEY_PREFIX + provider.name() + ":" + eventId;
    }
}
