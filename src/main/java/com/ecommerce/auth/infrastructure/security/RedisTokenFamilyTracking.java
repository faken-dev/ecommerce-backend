package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.TokenFamilyTracking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisTokenFamilyTracking implements TokenFamilyTracking {

    private final StringRedisTemplate redisTemplate;

    private static final String FAMILY_PREFIX = "token:family:";
    /** TTL = refresh token expiration (7 days). Family key expires when all tokens expire. */
    private static final Duration FAMILY_TTL = Duration.ofDays(7);

    @Override
    public void recordGeneration(UUID userId, long generation) {
        String key = familyKey(userId);
        redisTemplate.opsForValue().set(key, String.valueOf(generation), FAMILY_TTL);
    }

    @Override
    public boolean isGenerationValid(UUID userId, long generation) {
        String key = familyKey(userId);
        String currentGenStr = redisTemplate.opsForValue().get(key);

        // No family exists → first token → valid
        if (currentGenStr == null) {
            return true;
        }

        long currentGen = Long.parseLong(currentGenStr);
        // Token generation must match current family generation
        return generation == currentGen;
    }

    @Override
    public long getCurrentGeneration(UUID userId) {
        String key = familyKey(userId);
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0 : Long.parseLong(val);
    }

    @Override
    public void revokeFamily(UUID userId) {
        redisTemplate.delete(familyKey(userId));
    }

    private String familyKey(UUID userId) {
        return FAMILY_PREFIX + userId;
    }
}
