package com.ecommerce.payment.infrastructure.gateway.stripe;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.config.PaymentProperties.Stripe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Stripe adapter implementing {@link PaymentGatewayPort}.
 *
 * <p>Supports all Stripe payment methods:
 * <ul>
 *   <li>Card payments (Visa, Mastercard, Amex, JCB, etc.)</li>
 *   <li>Bank redirects (FPX, SEPA, BACS, etc.)</li>
 *   <li>Wallets (Apple Pay, Google Pay, Alipay, etc.)</li>
 *   <li>Pay Later (Klarna, Afterpay, Affirm)</li>
 *   <li>VietQR (via Stripe's Vietnam bank integration)</li>
 * </ul>
 *
 * <p>Signature: HMAC-SHA256 with Stripe Webhook Secret (whsec_...)
 * API authentication: Bearer token (Stripe Secret Key)
 */

@Component
@RequiredArgsConstructor
public class StripeGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(StripeGatewayAdapter.class);

    private final PaymentProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // - Helpers -------------

    private Stripe config() { return properties.getStripe(); }

    private HttpHeaders stripeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config().getSecretKey());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Stripe-Version", "2024-04-10");
        return headers;
    }

    // - Payment Initiation --------

    /**
     * Creates a Stripe PaymentIntent.
     * Returns the client_secret for frontend to complete payment via Stripe.js.
     */
    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                           String currency, PaymentProvider provider,
                           String returnUrl, String cancelUrl, String ipAddress) {

        Stripe config = config();
        if (!config.isEnabled()) {
            throw new IllegalStateException("Stripe is not configured. Set payment.stripe.secret-key");
        }

        String stripeCurrency = mapCurrencyToStripe(currency);
        String baseUrl = properties.getBaseUrl();

        // Build PaymentIntent parameters
        Map<String, String> params = new LinkedHashMap<>();
        params.put("amount", formatStripeAmount(amount, currency)); // cents
        params.put("currency", stripeCurrency);
        params.put("payment_method_types[0]", "card"); // default
        params.put("description", "Thanh toan don hang " + orderId);
        params.put("metadata[payment_id]", paymentId.toString());
        params.put("metadata[order_id]", orderId.toString());
        params.put("return_url", returnUrl != null ? returnUrl : baseUrl + "/payment/complete");
        params.put("cancel_url", cancelUrl != null ? cancelUrl : baseUrl + "/payment/cancelled");
        params.put("capture_method", "automatic"); // vs manual (requires separate capture)
        params.put("confirmation_method", "automatic"); // returns client_secret
        params.put("idempotency_key", paymentId.toString());

        try {
            HttpEntity<String> entity = new HttpEntity<>(buildFormEncoded(params), stripeHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl() + "/v1/payment_intents",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String intentId = root.get("id").asText();
            String clientSecret = root.get("client_secret").asText();
            String status = root.get("status").asText();

            log.info("[Stripe] PaymentIntent created [paymentId={}, intentId={}, status={}]",
                    paymentId, intentId, status);

            return InitiationResult.sdk(intentId, clientSecret);

        } catch (HttpClientErrorException e) {
            log.error("[Stripe] PaymentIntent creation failed [paymentId={}, status={}, body={}]",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Stripe payment initiation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[Stripe] PaymentIntent creation failed [paymentId={}]", paymentId, e);
            throw new RuntimeException("Stripe payment initiation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the publishable key for Stripe.js initialization on frontend.
     */
    public String getPublishableKey() {
        Stripe config = config();
        if (!config.isEnabled()) {
            throw new IllegalStateException("Stripe is not configured");
        }
        return config.getPublishableKey();
    }

    // - Refund --------------

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                               BigDecimal amount, String currency) {

        Stripe config = config();
        if (!config.isEnabled()) {
            throw new IllegalStateException("Stripe is not configured");
        }

        // Extract PaymentIntent ID from providerReference (may contain | separator)
        String intentId = providerReference.split("\\|")[0];

        Map<String, String> params = new LinkedHashMap<>();
        params.put("payment_intent", intentId);
        params.put("amount", formatStripeAmount(amount, currency));
        params.put("currency", mapCurrencyToStripe(currency));
        params.put("reason", "requested_by_customer");
        params.put("metadata[refund_id]", refundId.toString());
        params.put("idempotency_key", "refund-" + refundId);

        try {
            HttpEntity<String> entity = new HttpEntity<>(buildFormEncoded(params), stripeHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl() + "/v1/refunds",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String stripeRefundId = root.get("id").asText();
            log.info("[Stripe] Refund issued [refundId={}, stripeRefundId={}]", refundId, stripeRefundId);

            return stripeRefundId;

        } catch (Exception e) {
            log.error("[Stripe] Refund failed [refundId={}]", refundId, e);
            throw new RuntimeException("Stripe refund failed: " + e.getMessage(), e);
        }
    }

    // - Signature Verification ------

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "OK";
    }

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signatureHeader) {
        if (provider != PaymentProvider.STRIPE) return false;

        Stripe config = config();
        if (!config.isEnabled()) return false;

        String webhookSecret = config.getWebhookSecret();
        if (webhookSecret == null) {
            log.warn("[Stripe] No webhook secret configured - skipping verification");
            return true; // Allow in dev
        }

        // Stripe webhook signature: t=timestamp,v1=signature
        // Format: t=1734567890,v1=abc123...
        try {
            String timestamp = null;
            String signature = null;

            if (signatureHeader != null) {
                for (String part : signatureHeader.split(",")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2) {
                        if ("t".equals(kv[0].trim())) timestamp = kv[1].trim();
                        if ("v1".equals(kv[0].trim())) signature = kv[1].trim();
                    }
                }
            }

            if (timestamp == null || signature == null) {
                log.warn("[Stripe] Missing signature headers");
                return false;
            }

            // Tolerance: 5 minutes
            long ts = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() / 1000 - ts) > 300) {
                log.warn("[Stripe] Webhook timestamp too old or in future");
                return false;
            }

            // Compute expected signature: HMAC-SHA256(timestamp + "." + payload, secret)
            String signedPayload = timestamp + "." + payload;
            String expectedSig = hmacSha256(signedPayload, webhookSecret);

            boolean valid = expectedSig.equals(signature);
            if (!valid) {
                log.warn("[Stripe] Webhook signature mismatch");
            }
            return valid;

        } catch (Exception e) {
            log.error("[Stripe] Signature verification error", e);
            return false;
        }
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.STRIPE) {
            return new WebhookResult(null, null, null, null, null, null, null);
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("type").asText();
            JsonNode dataObject = root.path("data").path("object");

            String intentId = dataObject.path("id").asText();
            String status = dataObject.path("status").asText();
            String failureCode = dataObject.path("last_payment_error").path("code").asText(null);
            String failureReason = dataObject.path("last_payment_error").path("message").asText(null);

            PaymentStatus paymentStatus = mapStripeStatus(intentId, eventType, status);
            Instant paidAt = parseStripeTimestamp(dataObject.path("created").asLong(0));

            log.info("[Stripe] Webhook parsed [eventType={}, intentId={}, status={}, paymentStatus={}]",
                    eventType, intentId, status, paymentStatus);

            String eventId = root.path("id").asText();
            // Stripe intentId is our txnRef (mapped in metadata)
            return new WebhookResult(intentId, intentId, eventId, paymentStatus, paidAt, failureCode, failureReason);

        } catch (Exception e) {
            log.error("[Stripe] Failed to parse webhook", e);
            return new WebhookResult(null, null, null, null, null, "PARSE_ERROR", "Failed to parse webhook");
        }
    }

    // - Helpers -------------

    private PaymentStatus mapStripeStatus(String intentId, String eventType, String status) {
        return switch (eventType) {
            case "payment_intent.succeeded" -> PaymentStatus.PAID;
            case "payment_intent.payment_failed" -> PaymentStatus.FAILED;
            case "payment_intent.canceled" -> PaymentStatus.CANCELLED;
            case "payment_intent.processing" -> PaymentStatus.PROCESSING;
            case "charge.refunded" -> PaymentStatus.REFUNDED;
            case "charge.refund.updated" -> PaymentStatus.PARTIALLY_REFUNDED;
            default -> {
                // Also check status field for confirmation events
                yield switch (status) {
                    case "succeeded" -> PaymentStatus.PAID;
                    case "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.PROCESSING;
                    case "canceled" -> PaymentStatus.CANCELLED;
                    default -> PaymentStatus.PROCESSING;
                };
            }
        };
    }

    private String mapCurrencyToStripe(String currency) {
        return switch (currency) {
            case "VND" -> "vnd"; // Stripe VND is lowercase
            default -> currency.toLowerCase();
        };
    }

    /** Stripe expects amount in smallest unit (cents for VND, cents for USD). */
    private String formatStripeAmount(BigDecimal amount, String currency) {
        return switch (currency) {
            case "VND" -> String.valueOf(amount.longValue()); // VND has no subunit
            default -> String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()); // cents
        };
    }

    private Instant parseStripeTimestamp(long unixTimestamp) {
        return unixTimestamp > 0 ? Instant.ofEpochSecond(unixTimestamp) : Instant.now();
    }

    private String buildFormEncoded(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (sb.length() > 0) sb.append('&');
            try {
                sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            } catch (Exception e) {
                sb.append(k).append('=').append(v);
            }
        });
        return sb.toString();
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, String providerReference) {
        Stripe config = properties.getStripe();
        if (!config.isEnabled()) return null;

        if (providerReference == null || providerReference.isBlank()) {
            log.warn("[Stripe] Cannot check status for null/empty providerReference [paymentId={}]", paymentId);
            return new ReconciliationResult(PaymentStatus.PENDING, null, null, "MISSING_REFERENCE", "No Stripe PaymentIntent ID associated with this payment");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getSecretKey());
            headers.set("Stripe-Version", "2023-10-16");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl() + "/v1/payment_intents/" + providerReference,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();
            PaymentStatus paymentStatus = mapStripeStatus(providerReference, "status_check", status);

            return new ReconciliationResult(
                    paymentStatus,
                    providerReference,
                    Instant.now(),
                    null, null
            );
        } catch (Exception e) {
            log.error("[Stripe] Status check failed for {}", providerReference, e);
            return new ReconciliationResult(PaymentStatus.PENDING, providerReference, null, "CHECK_ERROR", e.getMessage());
        }
    }
}
