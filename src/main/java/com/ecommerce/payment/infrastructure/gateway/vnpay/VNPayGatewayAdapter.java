package com.ecommerce.payment.infrastructure.gateway.vnpay;

import com.ecommerce.payment.application.port.PaymentGatewayPort;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.config.PaymentProperties.VNPay;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

/**
 * VNPay adapter implementing {@link PaymentGatewayPort}.
 *
 * <p>VNPay is a redirect-based payment gateway (Vietnam):
 * <ul>
 *   <li>User is redirected to VNPay hosted page</li>
 *   <li>VNPay returns result via GET redirect + IPN webhook</li>
 *   <li>Supports: ATM card, QR code, Visa/MasterCard, VNPay Wallet</li>
 * </ul>
 *
 * <p>Signature: HMAC-SHA256
 */

@Component
@RequiredArgsConstructor
public class VNPayGatewayAdapter implements PaymentGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(VNPayGatewayAdapter.class);

    private final PaymentProperties properties;
    private final VNPayConfig vnpayConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // - Payment Initiation --------

    @Override
    public InitiationResult initiate(UUID paymentId, UUID orderId, BigDecimal amount,
                          String currency, PaymentProvider provider,
                          String returnUrl, String cancelUrl, String ipAddress) {

        VNPay config = properties.getVnpay();
        if (!config.isEnabled()) {
            throw new IllegalStateException("VNPay is not configured. Set payment.vnpay.tmn-code and payment.vnpay.hash-secret");
        }

        String txnRef = paymentId.toString().replace("-", "");
        String orderInfo = "Thanh toan don hang " + orderId.toString().replace("-", "");
        String clientIp = (ipAddress == null || ipAddress.equals("0:0:0:0:0:0:0:1")) ? "127.0.0.1" : ipAddress;
        String fullReturnUrl = returnUrl != null ? returnUrl
                : properties.getBaseUrl() + "/api/v1/payments/vnpay-return";

        String paymentUrl = vnpayConfig.buildPaymentUrl(
                amount.longValue(),
                txnRef,
                orderInfo,
                fullReturnUrl,
                clientIp,
                config.getTmnCode(),
                config.getHashSecret(),
                config.getApiUrl(),
                config.getLocale(),
                config.getCurrency()
        );

        log.info("[VNPay] Payment URL generated [paymentId={}, amount={} {}]",
                paymentId, amount, currency);

        return InitiationResult.redirect(txnRef, paymentUrl);
    }

    // - Refund --------------

    @Override
    public String issueRefund(UUID refundId, String providerReference,
                             BigDecimal amount, String currency) {

        VNPay config = properties.getVnpay();
        if (!config.isEnabled()) {
            throw new IllegalStateException("VNPay is not configured");
        }

        String requestId = UUID.randomUUID().toString().replace("-", "");
        String signature = vnpayConfig.buildRefundSignature(
                requestId,
                "2.1.0",
                "refund",
                config.getTmnCode(),
                providerReference,
                String.valueOf(amount.longValue() * 100),
                "", // Original transaction date - empty for now
                "02",
                "system",
                config.getHashSecret()
        );

        Map<String, String> refundParams = new TreeMap<>();
        refundParams.put("vnp_RequestId", requestId);
        refundParams.put("vnp_Version", "2.1.0");
        refundParams.put("vnp_Command", "refund");
        refundParams.put("vnp_TmnCode", config.getTmnCode());
        refundParams.put("vnp_TransactionType", "02");
        refundParams.put("vnp_TxnRef", providerReference);
        refundParams.put("vnp_Amount", String.valueOf(amount.longValue() * 100));
        refundParams.put("vnp_OrderInfo", "Refund for order " + providerReference);
        refundParams.put("vnp_TransDate", ""); 
        refundParams.put("vnp_CreateBy", "system");
        refundParams.put("vnp_SecureHash", signature);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(refundParams, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(config.getRefundUrl(), entity, String.class);
            
            log.info("[VNPay] Refund response: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());
            String responseCode = root.path("vnp_ResponseCode").asText();
            
            if ("00".equals(responseCode)) {
                return "VNPAY-REFUND-" + refundId;
            } else {
                throw new RuntimeException("VNPay refund failed with code: " + responseCode);
            }
        } catch (Exception e) {
            log.error("[VNPay] Refund failed [refundId={}]", refundId, e);
            throw new RuntimeException("VNPay refund failed: " + e.getMessage(), e);
        }
    }

    // - Webhook --------------

    @Override
    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature) {
        if (provider != PaymentProvider.VNPAY) return false;
        try {
            Map<String, String> params = parseQueryParams(payload);
            if (params.isEmpty() && payload.trim().startsWith("{")) {
                params = objectMapper.readValue(payload, new TypeReference<>() {});
            }
            return vnpayConfig.verifyIpnSignature(params, properties.getVnpay().getHashSecret());
        } catch (Exception e) {
            log.error("[VNPay] Signature verification error", e);
            return false;
        }
    }

    @Override
    public String generateWebhookResponse(PaymentProvider provider, WebhookResult result) {
        return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
    }

    @Override
    public WebhookResult parseWebhookEvent(PaymentProvider provider, String payload, String signature) {
        try {
            Map<String, String> params = parseQueryParams(payload);
            if (!vnpayConfig.verifyIpnSignature(params, properties.getVnpay().getHashSecret())) {
                return new WebhookResult(null, null, null, null, null, "INVALID_SIGNATURE", "Invalid signature");
            }

            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            PaymentStatus status = mapResponseCode(responseCode);

            return new WebhookResult(
                    txnRef,
                    params.get("vnp_TransactionNo"),
                    params.get("vnp_TransactionNo"),
                    status,
                    parsePayDate(params.get("vnp_PayDate")),
                    "00".equals(responseCode) ? null : "VNPAY_" + responseCode,
                    getResponseMessage(responseCode)
            );
        } catch (Exception e) {
            return new WebhookResult(null, null, null, null, null, "PARSE_ERROR", e.getMessage());
        }
    }

    public WebhookResult handleReturnUrl(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        if (!vnpayConfig.verifyReturnSignature(params, properties.getVnpay().getHashSecret())) {
            return new WebhookResult(txnRef, null, null, null, null, "INVALID_SIGNATURE", "Invalid signature");
        }

        String responseCode = params.get("vnp_ResponseCode");
        return new WebhookResult(
                txnRef,
                params.get("vnp_TransactionNo"),
                params.get("vnp_TransactionNo"),
                mapResponseCode(responseCode),
                parsePayDate(params.get("vnp_PayDate")),
                "00".equals(responseCode) ? null : "VNPAY_" + responseCode,
                getResponseMessage(responseCode)
        );
    }

    // - Helpers --------------

    private PaymentStatus mapResponseCode(String code) {
        if ("00".equals(code)) return PaymentStatus.PAID;
        if ("24".equals(code)) return PaymentStatus.CANCELLED;
        return PaymentStatus.FAILED;
    }

    private String getResponseMessage(String code) {
        return switch (code) {
            case "00" -> "Success";
            case "24" -> "User cancelled";
            case "51" -> "Insufficient balance";
            default -> "Payment failed (code: " + code + ")";
        };
    }

    private Instant parsePayDate(String payDate) {
        if (payDate == null || payDate.length() != 14) return Instant.now();
        try {
            return LocalDateTime.parse(payDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> map = new HashMap<>();
        if (queryString == null || queryString.isBlank()) return map;
        for (String pair : queryString.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.length() > idx + 1 ? pair.substring(idx + 1) : "";
                try {
                    map.put(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    @Override
    public ReconciliationResult checkStatus(UUID paymentId, String orderId, 
                                           PaymentProvider provider, String providerReference) {
        VNPay config = properties.getVnpay();
        String txnRef = providerReference != null ? providerReference : paymentId.toString();
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        
        String signature = vnpayConfig.buildQuerySignature(
                UUID.randomUUID().toString(), "2.1.0", "querydr", config.getTmnCode(), 
                txnRef, now, now, "127.0.0.1", "Query " + txnRef, config.getHashSecret()
        );

        Map<String, Object> body = Map.of(
            "vnp_RequestId", UUID.randomUUID().toString(),
            "vnp_Version", "2.1.0",
            "vnp_Command", "querydr",
            "vnp_TmnCode", config.getTmnCode(),
            "vnp_TxnRef", txnRef,
            "vnp_OrderInfo", "Query " + txnRef,
            "vnp_TransactionDate", now,
            "vnp_CreateDate", now,
            "vnp_IpAddr", "127.0.0.1",
            "vnp_SecureHash", signature
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(config.getRefundUrl(), body, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String responseCode = root.path("vnp_ResponseCode").asText();
            String transStatus = root.path("vnp_TransactionStatus").asText();

            PaymentStatus status = ("00".equals(responseCode) && "00".equals(transStatus)) ? PaymentStatus.PAID : PaymentStatus.FAILED;
            return new ReconciliationResult(status, txnRef, Instant.now(), responseCode, transStatus);
        } catch (Exception e) {
            return new ReconciliationResult(PaymentStatus.PENDING, txnRef, null, null, null);
        }
    }
}
