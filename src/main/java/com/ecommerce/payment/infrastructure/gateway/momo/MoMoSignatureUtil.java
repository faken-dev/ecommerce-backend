package com.ecommerce.payment.infrastructure.gateway.momo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MoMo signature utilities (PAYJP + RSA/SHA256).
 * MoMo uses HMAC-SHA256 for most endpoints.
 */

@Component
public class MoMoSignatureUtil {
    private static final Logger log = LoggerFactory.getLogger(MoMoSignatureUtil.class);

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates a MoMo signature for payment request.
     *
     * MoMo signature = HMAC-SHA256(rawHash, secretKey)
     * rawHash = concatenation of sorted key-value pairs: key1=val1&key2=val2...
     */
    public String generateSignature(Map<String, String> params, String secretKey) {
        // MoMo v2 uses alphabetical sorting for its signature raw hash.
        // We must ensure only the required parameters are in the map before calling this.
        Map<String, String> sorted = new TreeMap<>(params);

        StringBuilder rawHash = new StringBuilder();
        sorted.forEach((key, value) -> {
            if (!key.equals("signature")) {
                if (rawHash.length() > 0) rawHash.append('&');
                rawHash.append(key).append('=').append(value == null ? "" : value);
            }
        });

        String hashString = rawHash.toString();
        log.debug("[MoMo] Raw hash string: {}", hashString);
        return hmacSha256(hashString, secretKey);
    }

    /**
     * Verifies MoMo callback signature.
     *
     * MoMo sends signature in "signature" field.
     * rawHash includes all fields EXCEPT "signature".
     */
    public boolean verifyCallbackSignature(Map<String, String> params, String secretKey) {
        String receivedSignature = params.get("signature");
        if (receivedSignature == null) {
            log.warn("[MoMo] No signature in callback");
            return false;
        }

        String computedSignature = generateSignature(params, secretKey);
        boolean valid = computedSignature.equals(receivedSignature);

        if (!valid) {
            log.warn("[MoMo] Signature mismatch [received={}, computed={}]",
                    receivedSignature, computedSignature);
        }
        return valid;
    }

    /**
     * Generates a unique request ID for MoMo API.
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    /**
     * Generates a unique order ID with MoMo prefix.
     */
    public String generateOrderId(UUID paymentId) {
        return "MOMO_" + paymentId.toString().replace("-", "").toUpperCase();
    }

    /**
     * Creates raw hash map for MoMo payment request.
     */
    public Map<String, String> buildPaymentRequest(
            String partnerCode,
            String accessKey,
            String requestId,
            String orderId,
            long amount,
            String orderInfo,
            String redirectUrl,
            String ipnUrl,
            String requestType,
            String extraData) {

        Map<String, String> params = new HashMap<>();
        params.put("partnerCode", partnerCode);
        params.put("accessKey", accessKey);
        params.put("requestId", requestId);
        params.put("amount", String.valueOf(amount));
        params.put("orderId", orderId);
        params.put("orderInfo", orderInfo);
        params.put("ipnUrl", ipnUrl);
        params.put("redirectUrl", redirectUrl != null ? redirectUrl : "");
        params.put("requestType", requestType != null ? requestType : "captureWallet");
        params.put("extraData", extraData != null ? extraData : "");

        return params;
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
