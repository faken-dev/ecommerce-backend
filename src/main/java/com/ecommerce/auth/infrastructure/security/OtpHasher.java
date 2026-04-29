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
 * KHĂ„â€Ă¢â‚¬ÂNG dĂ„â€Ă‚Â¹ng BCrypt cho OTP vĂ„â€Ă‚Â¬:
 * 1. OTP fixed-length (6 chÄ‚Â¡Ă‚Â»Ă‚Â¯ sÄ‚Â¡Ă‚Â»Ă¢â‚¬Ëœ) Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ BCrypt "adaptive cost" lĂ„â€Ă‚Â  overkill, tÄ‚Â¡Ă‚Â»Ă¢â‚¬Ëœn CPU
 * 2. OTP Ä‚â€Ă¢â‚¬ËœĂ„â€Ă‚Â£ Ä‚â€Ă¢â‚¬ËœÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă‚Â£c generated bÄ‚Â¡Ă‚ÂºĂ‚Â±ng SecureRandom Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ khĂ„â€Ă‚Â´ng cÄ‚Â¡Ă‚ÂºĂ‚Â§n "password stretching"
 * 3. BCrypt hash output 60 kĂ„â€Ă‚Â½ tÄ‚Â¡Ă‚Â»Ă‚Â± Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ lĂ„â€Ă‚Â£ng phĂ„â€Ă‚Â­ storage
 *
 * DĂ„â€Ă‚Â¹ng HMAC-SHA256 vĂ„â€Ă‚Â¬:
 * - Fast, constant-size hash (32 bytes)
 * - Keyed hash Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ attacker cÄ‚Â¡Ă‚ÂºĂ‚Â§n biÄ‚Â¡Ă‚ÂºĂ‚Â¿t secret mÄ‚Â¡Ă‚Â»Ă¢â‚¬Âºi forge Ä‚â€Ă¢â‚¬ËœÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă‚Â£c
 * - Constant-time comparison Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă†â€™ trĂ„â€Ă‚Â¡nh timing attacks
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
     * Constant-time string comparison Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â prevents timing attacks.
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
