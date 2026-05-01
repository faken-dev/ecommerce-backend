package com.ecommerce.payment.infrastructure.gateway.zalopay;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.config.PaymentProperties.ZaloPay;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * ZaloPay adapter implementing {@link PaymentGatewayPort}.
 *
 * <p>Supports:
 * <ul>
 *   <li>ZaloPay Wallet</li>
 *   <li>ZaloPay QR (Vietnamese QR payments)</li>
 *   <li>ATM card via ZaloPay</li>
 * </ul>
 *
 * <p>Signature: HMAC-SHA256
 */

@Component
@RequiredArgsConstructor
public class ZaloPayGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(ZaloPayGatewayAdapter.class);

    private final PaymentProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // - Signature -------------

    private String generateSignature(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }

    // - Payment Initiation --------

    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                           String currency, PaymentProvider provider,
                           String returnUrl, String cancelUrl, String ipAddress) {

        ZaloPay config = properties.getZalopay();
        if (!config.isEnabled()) {
            throw new IllegalStateException("ZaloPay is not configured");
        }

        String appIdStr = config.getAppId();
        // ZaloPay format: yyMMdd_xxxx. We use the full paymentId (32 hex chars) as xxxx
        // total length: 6 (date) + 1 (_) + 32 (uuid) = 39 characters (max 40)
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String datePrefix = formatter.format(new Date());
        String orderIdZalo = datePrefix + "_" + paymentId.toString().replace("-", "");
        
        long amountInLong = amount.longValue();
        long appTime = System.currentTimeMillis();
        String description = "Thanh toan don hang #" + orderId;
        
        Map<String, String> embedDataMap = new HashMap<>();
        String finalReturnUrl = returnUrl != null ? returnUrl : properties.getBaseUrl() + "/api/v1/payments/zalopay-return";
        log.info("[ZaloPay] Using redirect URL: {}", finalReturnUrl);
        embedDataMap.put("redirecturl", finalReturnUrl);
        
        String embedData;
        String item = "[]";
        try {
            embedData = objectMapper.writeValueAsString(embedDataMap);
        } catch (Exception e) {
            embedData = "{\"redirecturl\":\"" + (returnUrl != null ? returnUrl : properties.getBaseUrl() + "/api/v1/payments/zalopay-return") + "\"}";
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("app_id", Integer.parseInt(appIdStr));
        payload.put("app_trans_id", orderIdZalo);
        payload.put("app_user", paymentId.toString());
        payload.put("app_time", appTime);
        payload.put("amount", amountInLong);
        payload.put("item", item);
        payload.put("embed_data", embedData);
        payload.put("description", description);
        payload.put("bank_code", "");
        payload.put("callback_url", properties.getBaseUrl() + "/api/v1/payments/webhook/zalopay");

        // MAC = hmac_sha256(key1, app_id + "|" + app_trans_id + "|" + app_user + "|" + amount + "|" + app_time + "|" + embed_data + "|" + item)
        String macData = appIdStr + "|" + orderIdZalo + "|" + paymentId.toString() + "|" + amountInLong + "|" + appTime + "|" + embedData + "|" + item;
        payload.put("mac", generateSignature(macData, config.getKey1()));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getApiUrl() + "/v2/create",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            int returnCode = root.get("return_code").asInt();

            if (returnCode == 1) {
                String zpTransToken = root.path("zp_trans_token").asText();
                String orderUrl = root.path("order_url").asText();
                log.info("[ZaloPay] Payment initiated [paymentId={}, orderIdZalo={}, zpTransToken={}, orderUrl={}]",
                        paymentId, orderIdZalo, zpTransToken, orderUrl);
                return InitiationResult.redirect(orderIdZalo, orderUrl);
            } else {
                throw new RuntimeException("ZaloPay error: " + root.path("return_message").asText());
            }

        } catch (Exception e) {
            log.error("[ZaloPay] Payment initiation failed [paymentId={}]", paymentId, e);
            throw new RuntimeException("ZaloPay payment initiation failed: " + e.getMessage(), e);
        }
    }

    // - Refund --------------

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                               BigDecimal amount, String currency) {

        ZaloPay config = properties.getZalopay();
        if (!config.isEnabled()) {
            throw new IllegalStateException("ZaloPay is not configured");
        }

        String appIdStr = config.getAppId();
        String refundIdZalo = "REFUND" + System.currentTimeMillis() + refundId.toString().substring(0, 8).toUpperCase();
        long amountInLong = amount.longValue();
        long timestamp = System.currentTimeMillis();

        String description = "Refund for order " + providerReference;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("app_id", Long.parseLong(appIdStr));
        payload.put("zp_trans_id", providerReference);
        payload.put("amount", amountInLong);
        payload.put("description", description);
        payload.put("timestamp", timestamp);

        // MAC = hmac_sha256(key1, app_id + "|" + zp_trans_id + "|" + amount + "|" + description + "|" + timestamp)
        String macData = appIdStr + "|" + providerReference + "|" + amountInLong + "|" + description + "|" + timestamp;
        payload.put("mac", generateSignature(macData, config.getKey1()));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getRefundUrl(), HttpMethod.POST, entity, JsonNode.class);

            JsonNode root = response.getBody();
            int returnCode = root.get("return_code").asInt();

            if (returnCode == 1) {
                log.info("[ZaloPay] Refund issued [refundId={}, zalopayRefundId={}]", refundId, refundIdZalo);
                return "ZLP-REFUND-" + refundIdZalo;
            } else {
                throw new RuntimeException("ZaloPay refund failed: " + root.path("return_message").asText());
            }
        } catch (Exception e) {
            log.error("[ZaloPay] Refund failed [refundId={}]", refundId, e);
            throw new RuntimeException("ZaloPay refund failed: " + e.getMessage(), e);
        }
    }

    // - Signature Verification ------

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "{\"return_code\":1,\"return_message\":\"success\"}";
    }

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.ZALOPAY) return false;
        ZaloPay config = properties.getZalopay();
        if (!config.isEnabled()) return false;

        try {
            JsonNode node = objectMapper.readTree(payload);
            String dataStr = node.path("data").asText();
            String receivedMac = node.path("mac").asText();
            String computedMac = generateSignature(dataStr, config.getKey2());
            return computedMac.equalsIgnoreCase(receivedMac);
        } catch (Exception e) {
            log.error("[ZaloPay] Signature verification failed", e);
            return false;
        }
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.ZALOPAY) {
            return new WebhookResult(null, null, null, null, null, null, null);
        }

        try {
            JsonNode node = objectMapper.readTree(payload);
            String dataStr = node.path("data").asText();
            String receivedMac = node.path("mac").asText();

            // Verify MAC
            if (!generateSignature(dataStr, config().getKey2()).equalsIgnoreCase(receivedMac)) {
                log.warn("[ZaloPay] Invalid webhook MAC");
                return new WebhookResult(null, null, null, null, null, "INVALID_SIGNATURE", "Invalid signature");
            }

            JsonNode dataNode = objectMapper.readTree(dataStr);
            String appTransId = dataNode.path("app_trans_id").asText();
            int returnCode = dataNode.path("return_code").asInt();

            PaymentStatus status = mapReturnCode(returnCode);
            String failureCode = returnCode == 1 ? null : "ZALOPAY_" + returnCode;
            String failureReason = getReturnMessage(returnCode);

            Instant paidAt = returnCode == 1 ? Instant.now() : null;

            log.info("[ZaloPay] Webhook parsed [appTransId={}, returnCode={}, status={}]",
                    appTransId, returnCode, status);

            String zpTransId = dataNode.path("zp_trans_id").asText();
            // ZaloPay app_trans_id is our txnRef
            return new WebhookResult(appTransId, zpTransId, null, status, paidAt, failureCode, failureReason);

        } catch (Exception e) {
            log.error("[ZaloPay] Failed to parse webhook", e);
            return new WebhookResult(null, null, null, null, null, "PARSE_ERROR", "Failed to parse webhook");
        }
    }

    private ZaloPay config() { return properties.getZalopay(); }

    private PaymentStatus mapReturnCode(int code) {
        return switch (code) {
            case 1 -> PaymentStatus.PAID;
            case 2 -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    private String getReturnMessage(int code) {
        return switch (code) {
            case 1 -> "Payment successful";
            case 2 -> "Payment pending";
            case 3 -> "Payment rejected";
            default -> "Payment failed (code: " + code + ")";
        };
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, String providerReference) {
        log.info("[ZaloPay] Checking status for payment {}/{}", paymentId, providerReference);
        
        ZaloPay config = properties.getZalopay();
        if (!config.isEnabled()) {
            throw new IllegalStateException("ZaloPay is not configured");
        }

        String appTransId = providerReference != null ? providerReference : orderId;
        String appId = config.getAppId();
        String key1 = config.getKey1();

        String data = appId + "|" + appTransId + "|" + key1;
        String mac = generateSignature(data, key1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("app_id", Long.parseLong(appId));
        requestBody.put("app_trans_id", appTransId);
        requestBody.put("mac", mac);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            StringBuilder sb = new StringBuilder();
            requestBody.forEach((k, v) -> {
                if (sb.length() > 0) sb.append('&');
                try {
                    sb.append(k).append('=').append(URLEncoder.encode(String.valueOf(v), StandardCharsets.UTF_8));
                } catch (Exception e) {}
            });
            
            HttpEntity<String> entity = new HttpEntity<>(sb.toString(), headers);

            // query endpoint: https://sb-openapi.zalopay.vn/v2/query
            String queryUrl = config.getApiUrl() + "/v2/query";

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    queryUrl, HttpMethod.POST, entity, JsonNode.class);

            JsonNode root = response.getBody();
            int returnCode = root.path("return_code").asInt();
            
            PaymentStatus status = mapReturnCode(returnCode);
            String failureReason = getReturnMessage(returnCode);

            return new ReconciliationResult(status, appTransId, Instant.now(), "ZALOPAY_" + returnCode, failureReason);
        } catch (ResourceAccessException e) {
            log.warn("[ZaloPay] Gateway unreachable ({}). Skipping status check for {}", 
                    e.getMessage(), appTransId);
            return new ReconciliationResult(PaymentStatus.PENDING, appTransId, null, "GATEWAY_UNREACHABLE", e.getMessage());
        } catch (Exception e) {
            log.error("[ZaloPay] Failed to check status for {}", appTransId, e);
            return new ReconciliationResult(PaymentStatus.PENDING, appTransId, null, "CHECK_ERROR", e.getMessage());
        }
    }

    /**
     * Fetches the list of supported banks from ZaloPay.
     * Based on the official sample provided by ZaloPay.
     */
    public JsonNode getBankList() {
        ZaloPay config = properties.getZalopay();
        String appId = config.getAppId();
        String reqTime = String.valueOf(System.currentTimeMillis());
        String data = appId + "|" + reqTime;
        String mac = generateSignature(data, config.getKey1());

        String endpoint = "https://sbgateway.zalopay.vn/api/getlistmerchantbanks";
        String url = String.format("%s?appid=%s&reqtime=%s&mac=%s", endpoint, appId, reqTime, mac);

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[ZaloPay] Failed to fetch bank list", e);
            throw new RuntimeException("Failed to fetch ZaloPay bank list", e);
        }
    }

    /**
     * Handles the redirect back from ZaloPay after payment.
     */
    public WebhookResult handleReturnUrl(Map<String, String> params) {
        ZaloPay config = properties.getZalopay();
        String appTransId = params.get("apptransid");
        
        // checksum = hmac_sha256(key2, appid + "|" + apptransid + "|" + pmcid + "|" + bankcode + "|" + amount + "|" + discountamount + "|" + status)
        String data = params.get("appid") + "|" +
                      params.get("apptransid") + "|" +
                      params.get("pmcid") + "|" +
                      params.get("bankcode") + "|" +
                      params.get("amount") + "|" +
                      params.get("discountamount") + "|" +
                      params.get("status");
        
        String expectedMac = generateSignature(data, config.getKey2());
        String receivedMac = params.get("checksum");

        if (expectedMac == null || !expectedMac.equalsIgnoreCase(receivedMac)) {
            log.warn("[ZaloPay] Invalid return checksum for {}", appTransId);
            return new WebhookResult(appTransId, null, null, null, null, "INVALID_SIGNATURE", "Invalid checksum");
        }

        int status = Integer.parseInt(params.get("status"));
        PaymentStatus paymentStatus = (status == 1) ? PaymentStatus.PAID : PaymentStatus.FAILED;

        return new WebhookResult(
                appTransId,
                null, // providerReference not directly in redirect params
                null,
                paymentStatus,
                Instant.now(),
                status == 1 ? null : "ZALOPAY_" + status,
                status == 1 ? "Success" : "Failed"
        );
    }
}
