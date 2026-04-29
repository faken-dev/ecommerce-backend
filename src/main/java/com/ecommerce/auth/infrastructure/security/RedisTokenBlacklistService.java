package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis implementation cÄ‚Â¡Ă‚Â»Ă‚Â§a TokenBlacklistService.
 * DĂ„â€Ă‚Â¹ng chung StringRedisTemplate vÄ‚Â¡Ă‚Â»Ă¢â‚¬Âºi RedisSpamProtection.
 *
 * Key format: "auth:blacklist:{sha256hash}" Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â token Ä‚â€Ă¢â‚¬ËœÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă‚Â£c hash trÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă¢â‚¬Âºc khi lĂ„â€Ă‚Â m key
 * Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă†â€™ trĂ„â€Ă‚Â¡nh dĂ„â€Ă‚Â¹ng raw JWT (200+ bytes) lĂ„â€Ă‚Â m key, tiÄ‚Â¡Ă‚ÂºĂ‚Â¿t kiÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¡m memory vĂ„â€Ă‚Â  bÄ‚Â¡Ă‚ÂºĂ‚Â£o mÄ‚Â¡Ă‚ÂºĂ‚Â­t hÄ‚â€ Ă‚Â¡n.
 * DĂ„â€Ă‚Â¹ng TTL = remaining token validity Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă†â€™ Redis tÄ‚Â¡Ă‚Â»Ă‚Â± cleanup.
 */
@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final TokenHasher tokenHasher;

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    @Override
    public void blacklist(String accessToken, long ttlMs) {
        if (ttlMs <= 0) {
            return;
        }
        String tokenHash = tokenHasher.hash(accessToken);
        String key = BLACKLIST_PREFIX + tokenHash;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(ttlMs));
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        String tokenHash = tokenHasher.hash(accessToken);
        String key = BLACKLIST_PREFIX + tokenHash;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
