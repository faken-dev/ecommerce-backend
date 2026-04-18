package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis implementation của TokenBlacklistService.
 * Dùng chung StringRedisTemplate với RedisSpamProtection.
 *
 * Key format: "auth:blacklist:{sha256hash}" — token được hash trước khi làm key
 * để tránh dùng raw JWT (200+ bytes) làm key, tiết kiệm memory và bảo mật hơn.
 * Dùng TTL = remaining token validity để Redis tự cleanup.
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
