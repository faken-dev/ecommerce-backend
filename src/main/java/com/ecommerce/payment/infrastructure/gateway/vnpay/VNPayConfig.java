package com.ecommerce.payment.infrastructure.gateway.vnpay;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPay SHA-256 signature utility.
 * VNPay uses HMAC-SHA256 with k=VNP_HASH_SECRET, not RSA.
 */

@Component
@RequiredArgsConstructor
public class VNPayConfig {
    private static final Logger log = LoggerFactory.getLogger(VNPayConfig.class);

    private static final String HASH_ALGORITHM = "HmacSHA512";

    /**
     * Builds the VNPay payment URL.
     *
     * @param amount        Amount in VND (e.g. 100000)
     * @param orderId       Internal order/payment ID
     * @param orderInfo     Description text
     * @param returnUrl     URL to redirect after payment
     * @param ipAddress     Client IP
     * @param tmnCode       VNPay Terminal ID
     * @param hashSecret    VNPay Hash Secret
     * @param apiUrl        VNPay API endpoint
     * @param locale        vn | en
     * @param currency      VND
     * @return full VNPay payment URL
     */
    public String buildPaymentUrl(
            long amount,
            String orderId,
            String orderInfo,
            String returnUrl,
            String ipAddress,
            String tmnCode,
            String hashSecret,
            String apiUrl,
            String locale,
            String currency) {

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", currency);
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", locale);
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSha512(hashData.toString(), hashSecret);
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return apiUrl + "?" + queryUrl;
    }

    /**
     * Verifies VNPay return URL signature.
     */
    public boolean verifyReturnSignature(Map<String, String> params, String hashSecret) {
        String receivedSignature = params.get("vnp_SecureHash");
        if (receivedSignature == null) return false;

        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("vnp_SecureHashType");
        sortedParams.remove("vnp_SecureHash");

        StringBuilder signData = new StringBuilder();
        sortedParams.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                if (signData.length() > 0) signData.append('&');
                try {
                    // VNPay 2.1.0 requires URL encoding for the hash data
                    // Use US_ASCII to get '+' for spaces, which is standard for VNPay
                    signData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    signData.append(key).append('=').append(value);
                }
            }
        });

        String expectedSignature = hmacSha512(signData.toString(), hashSecret);
        boolean isValid = expectedSignature.equalsIgnoreCase(receivedSignature);
        if (!isValid) {
            log.warn("[VNPay] Signature mismatch! Expected: {}, Received: {}, Data: {}", 
                    expectedSignature, receivedSignature, signData);
        }
        return isValid;
    }

    /**
     * Verifies VNPay IPN/webhook signature.
     * Note: IPN parameters might be encoded differently.
     */
    public boolean verifyIpnSignature(Map<String, String> params, String hashSecret) {
        // For VNPay 2.1.0, the hash data for both Return and IPN should be the same raw key=value pairs joined by &
        // sorted by key name.
        return verifyReturnSignature(params, hashSecret);
    }

    /**
     * Builds refund URL and signature for VNPay.
     */
    // public String buildRefundSignature(
    //         String txnRef, String amount, String transDate,
    //         String transType, String user, String hashSecret) {

    //     StringBuilder signData = new StringBuilder();
    //     signData.append(transType).append('|')
    //             .append(txnRef).append('|')
    //             .append(amount).append('|')
    //             .append(transDate).append('|')
    //             .append(user).append('|')
    //             .append("");
    //     return hmacSha512(signData.toString(), hashSecret);
    // }
    public String buildRefundSignature(
        String requestId,   // thĂ„â€Ă‚Âªm mới
        String version,     // thĂ„â€Ă‚Âªm mới
        String command,     // thĂ„â€Ă‚Âªm mới
        String tmnCode,     // thĂ„â€Ă‚Âªm mới
        String txnRef,
        String amount,
        String transDate,
        String transType,
        String user,
        String hashSecret) {

    String signData = requestId + "|"
            + version    + "|"
            + command    + "|"
            + tmnCode    + "|"
            + transType  + "|"
            + txnRef     + "|"
            + amount     + "|"
            + transDate  + "|"
            + user;

    return hmacSha512(signData, hashSecret);
}

    /**
     * Builds query URL signature for VNPay transaction query.
     */
    public String buildQuerySignature(
            String requestId, String version, String command, String tmnCode,
            String txnRef, String transDate, String createDate, String ipAddr,
            String orderInfo, String hashSecret) {

        StringBuilder signData = new StringBuilder();
        signData.append(requestId).append('|')
                .append(version).append('|')
                .append(command).append('|')
                .append(tmnCode).append('|')
                .append(txnRef).append('|')
                .append(transDate).append('|')
                .append(createDate).append('|')
                .append(ipAddr).append('|')
                .append(orderInfo);
        return hmacSha512(signData.toString(), hashSecret);
    }

    private String hmacSha512(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HASH_ALGORITHM);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA512", e);
        }
    }
}
