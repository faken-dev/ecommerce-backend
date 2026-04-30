package com.ecommerce.payment.application.port;

import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import java.time.Instant;

/**
 * Port for handling and parsing payment gateway webhooks.
 */
public interface PaymentWebhookHandler {

    boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature);

    String generateWebhookResponse(PaymentProvider provider, WebhookResult result);

    WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature);

    record WebhookResult(
            String txnRef,
            String providerReference,
            String webhookEventId,
            PaymentStatus status,
            Instant paidAt,
            String failureCode,
            String failureReason
    ) {}
}
