package com.ecommerce.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Hashing utility cho OTP codes.
 *
 * KHÔNG dùng BCrypt cho OTP vì:
 * 1. OTP fixed-length (6 chữ số) → BCrypt "adaptive cost" là overkill, tốn CPU
 * 2. OTP đã được generated bằng SecureRandom → không cần "password stretching"
 * 3. BCrypt hash output 60 ký tự → lãng phí storage
 *
 * Dùng HMAC-SHA256 vì:
 * - Fast, constant-size hash (32 bytes)
 * - Keyed hash → attacker cần biết secret mới forge được
 * - Constant-time comparison để tránh timing attacks
 */
@Component
public class OtpHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final byte[] secretKey;

    public OtpHasher(@Value("${app.otp.hmac-secret}") String hmacSecret) {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            throw new IllegalStateException(
                "app.otp.hmac-secret must be configured. " +
                "Generate with: openssl rand -base64 32");
        }
        this.secretKey = hmacSecret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Hash a raw OTP code.
     * @param rawOtp  6-digit OTP string (e.g. "123456")
     * @return SHA-256 hash as hex string (64 chars)
     */
    public String hash(String rawOtp) {
        try {
            // First HMAC with secret key, then SHA-256
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
            byte[] hmacBytes = mac.doFinal(rawOtp.getBytes(StandardCharsets.UTF_8));

            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(hmacBytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash OTP", e);
        }
    }

    /**
     * Verify a raw OTP against a stored hash.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param rawOtp      raw OTP from user input
     * @param storedHash  hash stored in DB
     * @return true if match
     */
    public boolean verify(String rawOtp, String storedHash) {
        String computed = hash(rawOtp);
        return constantTimeEquals(computed, storedHash);
    }

    /**
     * Constant-time string comparison — prevents timing attacks.
     * Even if attacker can measure response time, they can't infer
     * how many characters matched.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
