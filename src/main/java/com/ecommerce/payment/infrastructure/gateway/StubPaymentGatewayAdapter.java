package com.ecommerce.payment.infrastructure.gateway;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stub implementation of {@link PaymentGatewayPort} for local development
 * and when no real gateway credentials are configured.
 *
 * <p>To enable a real provider, replace this with:
 * <ul>
 *   <li>{@code StripeGatewayAdapter} - wraps Stripe Java SDK</li>
 *   <li>{@code PayPalGatewayAdapter} - wraps PayPal Java SDK</li>
 *   <li>{@code VnPayGatewayAdapter} - calls VNPay REST API</li>
 *   <li>{@code MoMoGatewayAdapter} - calls MoMo API</li>
 * </ul>
 *
 * <p>Production: check {@code PAYMENT_GATEWAY_ENABLED} env-var and throw
 * {@code PaymentGatewayException} if disabled but invoked.
 */

@Component
public class StubPaymentGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(StubPaymentGatewayAdapter.class);

    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                           String currency, PaymentProvider provider,
                           String returnUrl, String cancelUrl, String ipAddress) {
        log.info("[STUB] Initiating payment [paymentId={}, provider={}, amount={} {}]",
                paymentId, provider, amount, currency);

        // In stub mode, generate a fake provider reference
        String reference = "STUB-" + provider.name() + "-" + UUID.randomUUID();
        log.info("[STUB] Payment initiated [reference={}]", reference);

        // For COD, we don't need a redirect URL
        if (provider == PaymentProvider.COD) {
            return InitiationResult.redirect(reference, null);
        }

        return InitiationResult.redirect(reference, "http://localhost:3000/payment/stub?ref=" + reference);
    }

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                              BigDecimal amount, String currency) {
        log.info("[STUB] Issuing refund [refundId={}, originalRef={}, amount={} {}]",
                refundId, providerReference, amount, currency);
        return "STUB-REFUND-" + UUID.randomUUID();
    }

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "STUB_OK";
    }

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider,
                                          String payload, String signature) {
        // Stub accepts all - validate with real provider in production
        log.warn("[STUB] Webhook signature verification bypassed for provider {}", provider);
        return true;
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider,
                                           String payload, String signature) {
        log.warn("[STUB] Webhook parsing not implemented - configure real adapter");
        return new WebhookResult(null, null, null, null, null, null, null);
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, 
                                           String providerReference) {
        log.info("[STUB] Checking status for payment {}", paymentId);
        // Stub always returns PENDING to let the worker try again or just keep it as is
        return new ReconciliationResult(
                PaymentStatus.PENDING,
                providerReference,
                null, null, null
        );
    }
}
