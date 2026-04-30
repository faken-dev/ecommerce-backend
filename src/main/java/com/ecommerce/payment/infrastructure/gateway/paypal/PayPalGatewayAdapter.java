package com.ecommerce.payment.infrastructure.gateway.paypal;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.config.PaymentProperties.PayPal;
import com.ecommerce.shared.application.port.ExchangeRatePort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * PayPal adapter implementing {@link PaymentGatewayPort}.
 *
 * <p>Uses PayPal Orders API (v2) - supports:
 * <ul>
 *   <li>PayPal balance / bank account (default)</li>
 *   <li>Visa / Mastercard via PayPal</li>
 *   <li>PayPal Credit</li>
 * </ul>
 *
 * <p>Signature: OAuth2 Bearer token (client credentials flow).
 * Webhook verification: PayPal API POST /v1/notifications/verify-webhook-signature
 */

@Component
@RequiredArgsConstructor
public class PayPalGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(PayPalGatewayAdapter.class);

    private final PaymentProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExchangeRatePort exchangeRatePort;

    private volatile String cachedAccessToken;
    private volatile long tokenExpiresAt;

    // - OAuth2 Token (Client Credentials)--

    private String getAccessToken(PayPal config) {
        // Token valid for ~9 hours, cache it
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60_000) {
            return cachedAccessToken;
        }

        // Base64 encoding handled by setBasicAuth

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(config.getClientId(), config.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        String body = "grant_type=client_credentials";

        try {
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v1/oauth2/token",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode node = response.getBody();
            cachedAccessToken = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt(3600);
            tokenExpiresAt = System.currentTimeMillis() + expiresIn * 1000L;

            log.debug("[PayPal] Access token refreshed, expires in {}s", expiresIn);
            return cachedAccessToken;
        } catch (Exception e) {
            log.error("[PayPal] Failed to obtain access token", e);
            throw new RuntimeException("PayPal authentication failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders paypalHeaders(PayPal config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken(config));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        return headers;
    }

    // - Payment Initiation --------

    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                           String currency, PaymentProvider provider,
                           String returnUrl, String cancelUrl, String ipAddress) {

        PayPal config = properties.getPaypal();
        if (!config.isEnabled()) {
            throw new IllegalStateException("PayPal is not configured. Set payment.paypal.client-id and payment.paypal.client-secret");
        }

        String baseUrl = properties.getBaseUrl();
        String approvalUrl = baseUrl + "/payment/approved";
        String returnUrlFull = returnUrl != null ? returnUrl : approvalUrl;
        String cancelUrlFull = cancelUrl != null ? cancelUrl : baseUrl + "/payment/cancelled";

        // Build PayPal order payload using Map for reliable serialization
        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("intent", "CAPTURE");
        
        // Purchase units
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("reference_id", paymentId.toString());
        purchaseUnit.put("description", "Thanh toan don hang " + orderId);

        Map<String, String> amountObj = new HashMap<>();
        String targetCurrency = mapCurrency(currency);
        BigDecimal convertedAmount = exchangeRatePort.convert(amount, currency, targetCurrency);
        
        log.info("[PayPal] Currency conversion: {} {} -> {} {} (Amount: {})", 
                amount, currency, convertedAmount, targetCurrency, convertedAmount.setScale(2, RoundingMode.HALF_UP));

        amountObj.put("currency_code", targetCurrency);
        amountObj.put("value", convertedAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        purchaseUnit.put("amount", amountObj);

        orderPayload.put("purchase_units", List.of(purchaseUnit));

        // Application context (redirect URLs)
        Map<String, String> appContext = new HashMap<>();
        appContext.put("return_url", returnUrlFull);
        appContext.put("cancel_url", cancelUrlFull);
        appContext.put("brand_name", "E-Commerce");
        appContext.put("landing_page", "BILLING");
        appContext.put("user_action", "PAY_NOW");
        orderPayload.put("application_context", appContext);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderPayload, paypalHeaders(config));
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v2/checkout/orders",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            String orderIdPayPal = root.get("id").asText();
            String status = root.get("status").asText();

            // Extract approval URL from links
            String approvalLink = null;
            JsonNode links = root.get("links");
            if (links != null) {
                for (JsonNode link : links) {
                    if ("approve".equals(link.get("rel").asText())) {
                        approvalLink = link.get("href").asText();
                        break;
                    }
                }
            }

            log.info("[PayPal] Order created [paymentId={}, paypalOrderId={}, status={}, approvalUrl={}]",
                    paymentId, orderIdPayPal, status, approvalLink);

            return InitiationResult.redirect(orderIdPayPal, approvalLink);

        } catch (HttpClientErrorException e) {
            log.error("[PayPal] Order creation failed [paymentId={}, status={}]",
                    paymentId, e.getStatusCode());
            throw new RuntimeException("PayPal order creation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[PayPal] Order creation failed [paymentId={}]", paymentId, e);
            throw new RuntimeException("PayPal order creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Captures a PayPal order after buyer approves.
     * Call this when PayPal redirects back with the order ID.
     */
    public void captureOrder(String paypalOrderId) {
        PayPal config = properties.getPaypal();
        if (!config.isEnabled()) {
            throw new IllegalStateException("PayPal is not configured");
        }

        try {
            HttpEntity<String> entity = new HttpEntity<>(paypalHeaders(config));
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v2/checkout/orders/" + paypalOrderId + "/capture",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            String status = root.get("status").asText();
            log.info("[PayPal] Order captured [paypalOrderId={}, status={}]", paypalOrderId, status);

        } catch (Exception e) {
            log.error("[PayPal] Capture failed [paypalOrderId={}]", paypalOrderId, e);
            throw new RuntimeException("PayPal capture failed: " + e.getMessage(), e);
        }
    }

    // - Refund --------------

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                               BigDecimal amount, String currency) {

        PayPal config = properties.getPaypal();
        if (!config.isEnabled()) {
            throw new IllegalStateException("PayPal is not configured");
        }

        try {
            // First: find the capture ID from the original order
            // For simplicity, we refund by the PayPal order ID
            // In production: store capture_id when capturing the order
            String targetCurrency = mapCurrency(currency);
            BigDecimal convertedAmount = exchangeRatePort.convert(amount, currency, targetCurrency);

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("amount.currency_code", targetCurrency);
            payload.put("amount.value", convertedAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
            payload.put("note_to_payer", "Refund for order " + providerReference);

            HttpEntity<ObjectNode> entity = new HttpEntity<>(payload, paypalHeaders(config));

            // Refund endpoint uses capture_id
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v2/payments/captures/" + providerReference + "/refund",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            String refundIdPayPal = root.get("id").asText();
            log.info("[PayPal] Refund issued [refundId={}, paypalRefundId={}]", refundId, refundIdPayPal);

            return refundIdPayPal;

        } catch (Exception e) {
            log.error("[PayPal] Refund failed [refundId={}]", refundId, e);
            throw new RuntimeException("PayPal refund failed: " + e.getMessage(), e);
        }
    }

    // - Signature Verification ------

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "OK";
    }

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.PAYPAL) return false;

        PayPal config = properties.getPaypal();
        if (!config.isEnabled()) return false;

        try {
            JsonNode payloadNode = objectMapper.readTree(payload);
            String webhookId = config.getWebhookId();
            if (webhookId == null) {
                log.warn("[PayPal] No webhook ID configured, skipping verification");
                return true; // Allow in dev
            }

            String transmissionId = extractHeader(signature, "transmission_id");
            String transmissionTime = extractHeader(signature, "transmission_time");
            String certUrl = extractHeader(signature, "cert_url");
            String authAlgo = extractHeader(signature, "auth_algo");
            String transmissionSig = extractHeader(signature, "transmission_sig");

            ObjectNode verifyPayload = objectMapper.createObjectNode();
            verifyPayload.put("auth_algo", authAlgo != null ? authAlgo : "SHA256withRSA");
            verifyPayload.put("cert_url", certUrl != null ? certUrl : "");
            verifyPayload.put("transmission_id", transmissionId != null ? transmissionId : UUID.randomUUID().toString());
            verifyPayload.put("transmission_sig", transmissionSig != null ? transmissionSig : "");
            verifyPayload.put("transmission_time", transmissionTime != null ? transmissionTime : Instant.now().toString());
            verifyPayload.put("webhook_id", webhookId);
            verifyPayload.set("webhook_event", payloadNode);

            HttpEntity<ObjectNode> entity = new HttpEntity<>(verifyPayload, paypalHeaders(config));

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v1/notifications/verify-webhook-signature",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode result = response.getBody();
            boolean valid = "SUCCESS".equals(result.get("verification_status").asText());

            if (!valid) {
                log.warn("[PayPal] Webhook signature verification failed");
            }
            return valid;

        } catch (Exception e) {
            log.error("[PayPal] Signature verification error", e);
            return false;
        }
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.PAYPAL) {
            return new WebhookResult(null, null, null, null, null, null, null);
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event_type").asText();
            JsonNode resource = root.path("resource");

            String paypalOrderId = resource.path("id").asText();
            String status = resource.path("status").asText();

            PaymentStatus paymentStatus = mapPayPalStatus(status);
            String failureCode = null;
            String failureReason = null;

            if ("COMPLETED".equals(status) || "APPROVED".equals(status)) {
                // good
            } else if ("VOID".equals(status) || "REFUNDED".equals(status)) {
                failureCode = "PAYPAL_" + status;
                failureReason = "Order " + status.toLowerCase();
            } else if ("PAYER_ACTION_REQUIRED".equals(status) || "SAVED".equals(status)) {
                paymentStatus = PaymentStatus.PROCESSING;
            }

            log.info("[PayPal] Webhook parsed [eventType={}, paypalOrderId={}, status={}, paymentStatus={}]",
                    eventType, paypalOrderId, status, paymentStatus);

            String eventId = root.path("id").asText();
            // PayPal orderId (resource.id) is used as txnRef
            return new WebhookResult(paypalOrderId, paypalOrderId, eventId, paymentStatus, Instant.now(), failureCode, failureReason);

        } catch (Exception e) {
            log.error("[PayPal] Failed to parse webhook", e);
            return new WebhookResult(null, null, null, null, null, "PARSE_ERROR", "Failed to parse webhook");
        }
    }

    // - Helpers -------------

    private String extractHeader(String combined, String key) {
        if (combined == null || combined.isBlank()) return null;
        if (!combined.startsWith("{")) return combined; // Fallback if not JSON
        try {
            JsonNode node = objectMapper.readTree(combined);
            String jsonKey = switch (key) {
                case "transmission_id" -> "id";
                case "transmission_time" -> "time";
                case "cert_url" -> "cert";
                case "auth_algo" -> "algo";
                case "transmission_sig" -> "sig";
                default -> key;
            };
            return node.has(jsonKey) ? node.get(jsonKey).asText() : null;
        } catch (Exception e) {
            return combined;
        }
    }

    private PaymentStatus mapPayPalStatus(String status) {
        if (status == null) return PaymentStatus.FAILED;
        return switch (status) {
            case "APPROVED", "COMPLETED" -> PaymentStatus.PAID;
            case "VOID", "EXPIRED" -> PaymentStatus.CANCELLED;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            case "PENDING" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    private String mapCurrency(String currency) {
        return switch (currency) {
            case "VND" -> "USD"; // PayPal doesn't support VND directly - convert to USD
            default -> currency;
        };
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, String providerReference) {
        PayPal config = properties.getPaypal();
        if (!config.isEnabled()) return null;

        if (providerReference == null || providerReference.isBlank()) {
            log.warn("[PayPal] Cannot check status for null/empty providerReference [paymentId={}]", paymentId);
            return new ReconciliationResult(PaymentStatus.PENDING, null, null, "MISSING_REFERENCE", "No PayPal Order ID associated with this payment");
        }

        try {
            HttpEntity<String> entity = new HttpEntity<>(paypalHeaders(config));
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v2/checkout/orders/" + providerReference,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            String status = root.path("status").asText();
            PaymentStatus paymentStatus = mapPayPalStatus(status);

            return new ReconciliationResult(
                    paymentStatus,
                    providerReference,
                    Instant.now(),
                    null, null
            );
        } catch (Exception e) {
            log.error("[PayPal] Status check failed for {}", providerReference, e);
            return new ReconciliationResult(PaymentStatus.PENDING, providerReference, null, "CHECK_ERROR", e.getMessage());
        }
    }
}
