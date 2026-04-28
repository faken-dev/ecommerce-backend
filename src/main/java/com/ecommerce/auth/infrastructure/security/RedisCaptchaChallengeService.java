package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.CaptchaChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Captcha challenge logic based on login failure history.
 *
 * Captcha is triggered when:
 * - Failed login attempts reach a threshold
 * - Request rate from the same IP is suspiciously high
 */
@Service
@RequiredArgsConstructor
public class RedisCaptchaChallengeService implements CaptchaChallengeService {

    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_REQUIRED_PREFIX = "captcha:required:";
    private static final String IP_ATTEMPT_PREFIX = "login:ip-attempts:";

    @Value("${app.auth.captcha-threshold:3}")
    private int captchaThreshold;

    @Override
    public boolean isCaptchaRequired(UUID userId, String ipAddress, String email) {
        // Check if this IP has made too many attempts recently
        String ipKey = IP_ATTEMPT_PREFIX + ipAddress;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1L) {
            redisTemplate.expire(ipKey, Duration.ofMinutes(5));
        }

        // If IP has made suspicious number of attempts in last 5 minutes → captcha
        if (ipCount != null && ipCount >= 10) {
            return true;
        }

        // If user has failed attempts >= captchaThreshold → captcha required
        if (userId != null) {
            String userKey = CAPTCHA_REQUIRED_PREFIX + userId;
            Boolean exists = redisTemplate.hasKey(userKey);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean verifyCaptcha(String captchaToken, String ipAddress) {
        // In production, this would verify against Google reCAPTCHA / hCaptcha / Turnstile.
        // For now: accept any non-blank token as valid, and clear the captcha requirement flag.
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        // TO DO: integrate with actual captcha provider (e.g. Google reCAPTCHA)
        // For demonstration, just acknowledge and clear the flag.
        // Real implementation:
        //   boolean verified = verifyWithRecaptchaService(captchaToken, ipAddress);
        //   if (verified) clearCaptchaRequired(ipAddress);
        //   return verified;

        return true; // Replace with real verification
    }

    /** Called internally after successful captcha verification. */
    public void setCaptchaRequired(UUID userId, Duration ttl) {
        String key = CAPTCHA_REQUIRED_PREFIX + (userId != null ? userId : "ip");
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    /** Clear captcha requirement after successful verification. */
    public void clearCaptchaRequired(String ipAddress) {
        // Clear IP attempt counter
        redisTemplate.delete(IP_ATTEMPT_PREFIX + ipAddress);
    }
}
