package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.LoginAttemptTracking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisLoginAttemptService implements LoginAttemptTracking {

    private final StringRedisTemplate redisTemplate;

    private static final String ATTEMPTS_PREFIX   = "login:attempts:";
    private static final String LOCKOUT_PREFIX     = "login:lockout:";
    private static final String LOCKOUT_COUNT_PREFIX = "login:lockout-count:";

    @Override
    public void recordFailedAttempt(UUID userId, Duration baseDuration) {
        String attemptsKey = attemptsKey(userId);

        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        if (count == null) count = 1L;

        // Set TTL on first increment (daily reset)
        if (count == 1L) {
            redisTemplate.expire(attemptsKey, Duration.ofSeconds(secondsUntilMidnight()));
        }

        if (count >= maxAttempts()) {
            applyLockout(userId, baseDuration);
        }
    }

    @Override
    public void recordSuccessfulLogin(UUID userId) {
        String attemptsKey = attemptsKey(userId);
        redisTemplate.delete(attemptsKey);
    }

    @Override
    public boolean isLocked(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockoutKey(userId)));
    }

    @Override
    public long getRemainingLockoutSeconds(UUID userId) {
        Long ttl = redisTemplate.getExpire(lockoutKey(userId));
        return (ttl == null || ttl < 0) ? 0 : ttl;
    }

    @Override
    public void unlock(UUID userId) {
        redisTemplate.delete(lockoutKey(userId));
        redisTemplate.delete(attemptsKey(userId));
        redisTemplate.delete(lockoutCountKey(userId));
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Private helpers Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    private void applyLockout(UUID userId, Duration baseDuration) {
        String countKey = lockoutCountKey(userId);

        Long lockoutCount = redisTemplate.opsForValue().increment(countKey);
        if (lockoutCount == null) lockoutCount = 1L;

        // Exponential backoff: 1min Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ 2min Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ 4min Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ ... Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ max 30min
        long multiplier = Math.min(1L << (lockoutCount - 1), 1L << 5); // max 32x = ~32 min
        long ttlSeconds = Math.min(baseDuration.multipliedBy(multiplier).toSeconds(), 1800);

        redisTemplate.opsForValue().set(lockoutKey(userId), String.valueOf(lockoutCount),
                Duration.ofSeconds(ttlSeconds));

        // Reset attempt counter after lockout applied
        redisTemplate.delete(attemptsKey(userId));
    }

    private int maxAttempts() {
        // Injected config would be cleaner; hardcoded here for portability.
        // Wire via @Value if you need to externalize.
        return 5;
    }

    private String attemptsKey(UUID userId) {
        return ATTEMPTS_PREFIX + userId + ":" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String lockoutKey(UUID userId) {
        return LOCKOUT_PREFIX + userId;
    }

    private String lockoutCountKey(UUID userId) {
        return LOCKOUT_COUNT_PREFIX + userId;
    }

    private long secondsUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, midnight).getSeconds();
    }
}
