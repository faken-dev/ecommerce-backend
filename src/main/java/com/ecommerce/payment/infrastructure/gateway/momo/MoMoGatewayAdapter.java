package com.ecommerce.payment.infrastructure.gateway.momo;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.config.PaymentProperties.MoMo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * MoMo e-wallet adapter implementing {@link PaymentGatewayPort}.
 *
 * <p>MoMo supports:
 * <ul>
 *   <li>captureWallet - MoMo wallet payment</li>
 *   <li>payWithATM - ATM card payment</li>
 *   <li>payWithCC - Credit card payment</li>
 * </ul>
 *
 * <p>Signature: HMAC-SHA256
 */

@Component
@RequiredArgsConstructor
public class MoMoGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(MoMoGatewayAdapter.class);

    private final PaymentProperties properties;
    private final MoMoSignatureUtil signatureUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // - Payment Initiation --------

    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                            String currency, PaymentProvider provider,
                            String returnUrl, String cancelUrl, String ipAddress) {

        MoMo config = properties.getMomo();
        if (!config.isEnabled()) {
            throw new IllegalStateException("MoMo is not configured. Set payment.momo.partner-code, payment.momo.access-key, payment.momo.secret-key");
        }

        String requestId = signatureUtil.generateRequestId();
        String orderIdForMoMo = signatureUtil.generateOrderId(paymentId);
        // MoMo API expects VND amounts (no decimals).
        // longValueExact() throws ArithmeticException if fractional part is non-zero,
        // preventing silent data loss for non-VND currencies.
        long amountInLong = amount.longValueExact();
        String orderInfo = "Thanh toan don hang " + orderId;
        String fullReturnUrl = properties.getBaseUrl() + "/api/v1/payments/webhook/momo/return";
        String fullIpnUrl = properties.getBaseUrl() + "/api/v1/payments/webhook/momo";

        // Build param map for signing
        Map<String, String> params = signatureUtil.buildPaymentRequest(
                config.getPartnerCode(),
                config.getAccessKey(),
                requestId,
                orderIdForMoMo,
                amountInLong,
                orderInfo,
                fullReturnUrl,
                fullIpnUrl,
                "captureWallet", // default
                "" // extraData
        );

        // Generate signature
        String signature = signatureUtil.generateSignature(params, config.getSecretKey());
        params.put("signature", signature);
        params.put("lang", "vi");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            String resultCode = root.has("resultCode") ? root.get("resultCode").asText() : null;

            if ("0".equals(resultCode)) {
                String payUrl = root.has("payUrl") ? root.get("payUrl").asText() : null;
                log.info("[MoMo] Payment initiated [paymentId={}, momoOrderId={}, payUrl={}]",
                        paymentId, orderIdForMoMo, payUrl);
                
                return InitiationResult.redirect(orderIdForMoMo, payUrl);
            } else {
                String message = root.has("message") ? root.get("message").asText() : "Unknown error";
                throw new RuntimeException("MoMo payment initiation failed: " + resultCode + " - " + message);
            }

        } catch (Exception e) {
            log.error("[MoMo] Payment initiation failed [paymentId={}]", paymentId, e);
            throw new RuntimeException("MoMo payment initiation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the MoMo payment URL for client-side redirect/app deep link.
     */
    public String getPaymentUrl(UUID paymentId, UUID orderId, BigDecimal amount,
                                 String currency, String clientIp, String returnUrl) {
        MoMo config = properties.getMomo();
        if (!config.isEnabled()) {
            throw new IllegalStateException("MoMo is not configured");
        }

        String requestId = signatureUtil.generateRequestId();
        String orderIdForMoMo = signatureUtil.generateOrderId(paymentId);
        String orderInfo = "Thanh toan don hang " + orderId;
        String fullReturnUrl = returnUrl != null ? returnUrl
                : properties.getBaseUrl() + "/api/v1/payments/webhook/momo/return";
        String fullIpnUrl = properties.getBaseUrl() + "/api/v1/payments/webhook/momo";

        Map<String, String> params = signatureUtil.buildPaymentRequest(
                config.getPartnerCode(),
                config.getAccessKey(),
                requestId,
                orderIdForMoMo,
                amount.longValue(),
                orderInfo,
                fullReturnUrl,
                fullIpnUrl,
                "captureWallet",
                ""
        );

        params.put("signature", signatureUtil.generateSignature(params, config.getSecretKey()));
        params.put("lang", "vi");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl(), HttpMethod.POST, entity, JsonNode.class);

            JsonNode root = response.getBody();
            if ("0".equals(root.path("resultCode").asText())) {
                return root.path("payUrl").asText();
            }
            throw new RuntimeException("MoMo: " + root.path("message").asText());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get MoMo payment URL: " + e.getMessage(), e);
        }
    }

    // - Refund --------------

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                               BigDecimal amount, String currency) {

        MoMo config = properties.getMomo();
        if (!config.isEnabled()) {
            throw new IllegalStateException("MoMo is not configured");
        }

        String requestId = signatureUtil.generateRequestId();
        String orderId = "REFUND-" + System.currentTimeMillis() + "-" + refundId.toString().substring(0, 8);

        Map<String, String> params = new TreeMap<>();
        params.put("partnerCode", config.getPartnerCode());
        params.put("requestId", requestId);
        params.put("orderId", orderId);
        params.put("originalOrderId", providerReference);
        params.put("amount", String.valueOf(amount.longValue()));
        params.put("transId", providerReference); // MoMo transaction ID
        params.put("lang", "vi");

        String signature = signatureUtil.generateSignature(params, config.getSecretKey());
        params.put("signature", signature);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getRefundUrl(), HttpMethod.POST, entity, JsonNode.class);

            JsonNode root = response.getBody();
            String resultCode = root.path("resultCode").asText();

            if ("0".equals(resultCode)) {
                log.info("[MoMo] Refund successful [refundId={}, momoRefundId={}]",
                        refundId, orderId);
                return "MOMO-REFUND-" + orderId;
            } else {
                throw new RuntimeException("MoMo refund failed: " + resultCode + " - " + root.path("message").asText());
            }
        } catch (Exception e) {
            log.error("[MoMo] Refund failed [refundId={}]", refundId, e);
            throw new RuntimeException("MoMo refund failed: " + e.getMessage(), e);
        }
    }

    // - Signature Verification ------

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "{\"resultCode\":0,\"message\":\"Success\"}";
    }

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.MOMO) return false;

        try {
            JsonNode node = objectMapper.readTree(payload);
            Map<String, String> params = new HashMap<>();
            node.properties().forEach(e -> params.put(e.getKey(),
                    e.getValue().isValueNode() ? e.getValue().asText() : e.getValue().toString()));

            return signatureUtil.verifyCallbackSignature(params, properties.getMomo().getSecretKey());
        } catch (Exception e) {
            log.error("[MoMo] Signature verification failed", e);
            return false;
        }
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.MOMO) {
            return new WebhookResult(null, null, null, null, null, null, null);
        }

        try {
            JsonNode node = objectMapper.readTree(payload);

            // Verify signature
            Map<String, String> params = new HashMap<>();
            node.properties().forEach(e -> params.put(e.getKey(),
                    e.getValue().isValueNode() ? e.getValue().asText() : ""));
            if (!signatureUtil.verifyCallbackSignature(params, properties.getMomo().getSecretKey())) {
                log.warn("[MoMo] Invalid webhook signature");
                return new WebhookResult(null, null, null, null, null, "INVALID_SIGNATURE", "Invalid signature");
            }

            String resultCode = node.path("resultCode").asText();
            String orderId = node.path("orderId").asText();
            String requestId = node.path("requestId").asText();

            PaymentStatus status = mapResultCode(resultCode);
            String failureCode = "0".equals(resultCode) ? null : "MOMO_" + resultCode;
            String failureReason = getResultMessage(resultCode);

            // MoMo returns milliseconds timestamp
            long timestamp = node.path("responseTime").asLong(0);
            Instant paidAt = timestamp > 0 ? Instant.ofEpochMilli(timestamp) : Instant.now();

            // MoMo orderId is our txnRef (from signatureUtil.generateOrderId(paymentId))
            log.info("[MoMo] Webhook parsed [orderId={}, resultCode={}, status={}]",
                    orderId, resultCode, status);

            return new WebhookResult(orderId, orderId, requestId, status, paidAt, failureCode, failureReason);

        } catch (Exception e) {
            log.error("[MoMo] Failed to parse webhook", e);
            return new WebhookResult(null, null, null, null, null, "PARSE_ERROR", "Failed to parse webhook");
        }
    }

    // - Return URL Handler --------

    public WebhookResult handleReturnUrl(Map<String, String> params) {
        MoMo config = properties.getMomo();
        if (!config.isEnabled()) {
            return new WebhookResult(null, null, null, null, null, "NOT_CONFIGURED", "MoMo not configured");
        }

        if (!signatureUtil.verifyCallbackSignature(params, config.getSecretKey())) {
            log.warn("[MoMo] Invalid return URL signature");
            return new WebhookResult(null, null, null, null, null, "INVALID_SIGNATURE", "Invalid signature");
        }

        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");
        String transId = params.get("transId");
        String providerRef = transId != null && !transId.isEmpty() ? transId : orderId;

        PaymentStatus status = mapResultCode(resultCode);
        Instant paidAt = "0".equals(resultCode) ? Instant.now() : null;

        return new WebhookResult(orderId, providerRef, null, status, paidAt, null, null);
    }

    // - Helpers -------------

    private PaymentStatus mapResultCode(String code) {
        if (code == null) return PaymentStatus.FAILED;
        return switch (code) {
            case "0" -> PaymentStatus.PAID;
            case "1001", "1002", "1003", "1004" -> PaymentStatus.PROCESSING;
            case "1006", "1007" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.FAILED;
        };
    }

    private String getResultMessage(String code) {
        return switch (code) {
            case "0" -> "Payment successful";
            case "1001" -> "Invalid checksum";
            case "1002" -> "Order not found";
            case "1003" -> "Order already processed";
            case "1004" -> "Invalid amount";
            case "1006" -> "User cancelled";
            case "1007" -> "Transaction timeout";
            case "1010" -> "Order expired";
            default -> "Payment failed (code: " + code + ")";
        };
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, String providerReference) {
        log.info("[MoMo] Checking status for payment {}/{}", paymentId, providerReference);
        
        MoMo config = properties.getMomo();
        if (!config.isEnabled()) {
            throw new IllegalStateException("MoMo is not configured");
        }

        String momoOrderId = providerReference != null ? providerReference : signatureUtil.generateOrderId(paymentId);
        String requestId = signatureUtil.generateRequestId();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("partnerCode", config.getPartnerCode());
        requestBody.put("requestId", requestId);
        requestBody.put("orderId", momoOrderId);
        requestBody.put("lang", "vi");

        Map<String, String> signParams = new TreeMap<>();
        signParams.put("partnerCode", config.getPartnerCode());
        signParams.put("requestId", requestId);
        signParams.put("orderId", momoOrderId);
        signParams.put("accessKey", config.getAccessKey());
        
        String signature = signatureUtil.generateSignature(signParams, config.getSecretKey());
        requestBody.put("signature", signature);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            String queryUrl = config.getApiUrl().replace("/create", "/query"); // typical momo endpoint pattern

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    queryUrl, HttpMethod.POST, entity, JsonNode.class);

            JsonNode root = response.getBody();
            String resultCode = root.path("resultCode").asText();
            
            PaymentStatus status = mapResultCode(resultCode);
            String failureReason = getResultMessage(resultCode);

            return new ReconciliationResult(status, momoOrderId, Instant.now(), "MOMO_" + resultCode, failureReason);
        } catch (Exception e) {
            log.error("[MoMo] Failed to check status for {}", momoOrderId, e);
            return new ReconciliationResult(PaymentStatus.PENDING, momoOrderId, null, null, null);
        }
    }
}
