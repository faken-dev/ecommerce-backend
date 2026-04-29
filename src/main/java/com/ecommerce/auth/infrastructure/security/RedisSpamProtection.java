package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.SpamProtection;
import com.ecommerce.auth.domain.entity.OtpToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Redis implementation cÄ‚Â¡Ă‚Â»Ă‚Â§a SpamProtection.
 *
 * Key structure:
 *   otp:cooldown:{purpose}:{userId}   Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ cooldown window (TTL = cooldown seconds)
 *   otp:penalty:{purpose}:{userId}    Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ penalty window (TTL = penalty seconds)
 *   otp:penalty:warn:{purpose}:{userId} Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ warn count trong penalty window
 *   otp:daily:{purpose}:{userId}:{date} Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ sÄ‚Â¡Ă‚Â»Ă¢â‚¬Ëœ lÄ‚Â¡Ă‚ÂºĂ‚Â§n gÄ‚Â¡Ă‚Â»Ă‚Â­i trong ngĂ„â€Ă‚Â y (TTL = Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚ÂºĂ‚Â¿n nÄ‚Â¡Ă‚Â»Ă‚Â­a Ä‚â€Ă¢â‚¬ËœĂ„â€Ă‚Âªm)
 *   otp:block:{userId}                 Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ account blocked flag (no TTL = vÄ‚â€Ă‚Â©nh viÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¦n)
 */
@Service
@RequiredArgsConstructor
public class RedisSpamProtection implements SpamProtection {

    private final StringRedisTemplate redisTemplate;

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Key factories Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    private String cooldownKey(UUID userId, OtpToken.Purpose purpose) {
        return "otp:cooldown:" + purpose.name() + ":" + userId;
    }

    private String penaltyKey(UUID userId, OtpToken.Purpose purpose) {
        return "otp:penalty:" + purpose.name() + ":" + userId;
    }

    private String penaltyWarnKey(UUID userId, OtpToken.Purpose purpose) {
        return "otp:penalty:warn:" + purpose.name() + ":" + userId;
    }

    private String dailyKey(UUID userId, OtpToken.Purpose purpose) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "otp:daily:" + purpose.name() + ":" + userId + ":" + date;
    }

    private String blockedKey(UUID userId) {
        return "otp:block:" + userId;
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Helper: get TTL Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    private long getTtl(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return (ttl == null || ttl < 0) ? -1 : ttl;
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Cooldown Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    @Override
    public void setCooldownWindow(UUID userId, OtpToken.Purpose purpose, long cooldownSeconds) {
        if (cooldownSeconds <= 0) return;
        String key = cooldownKey(userId, purpose);
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(cooldownSeconds));
    }

    @Override
    public long getCooldownRemainingSeconds(UUID userId, OtpToken.Purpose purpose) {
        return getTtl(cooldownKey(userId, purpose));
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Penalty Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    @Override
    public void setPenaltyWindow(UUID userId, OtpToken.Purpose purpose, Duration penaltyDuration) {
        String key = penaltyKey(userId, purpose);
        redisTemplate.opsForValue().set(key, "1", penaltyDuration);
    }

    @Override
    public long getPenaltyTtl(UUID userId, OtpToken.Purpose purpose) {
        return getTtl(penaltyKey(userId, purpose));
    }

    @Override
    public long extendPenalty(UUID userId, OtpToken.Purpose purpose, Duration penaltyDuration) {
        String key = penaltyKey(userId, purpose);
        Long currentTtl = redisTemplate.getExpire(key);
        long current = (currentTtl == null || currentTtl < 0) ? 0 : currentTtl;
        long newTtl = current + penaltyDuration.getSeconds();

        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(newTtl));
        return newTtl;
    }

    @Override
    public int getPenaltyWarnCount(UUID userId, OtpToken.Purpose purpose) {
        String val = redisTemplate.opsForValue().get(penaltyWarnKey(userId, purpose));
        return val == null ? 0 : Integer.parseInt(val);
    }

    @Override
    public void incrementPenaltyWarnCount(UUID userId, OtpToken.Purpose purpose, long ttlSeconds) {
        String key = penaltyWarnKey(userId, purpose);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Daily limit Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    @Override
    public int getDailySendCount(UUID userId, OtpToken.Purpose purpose) {
        String val = redisTemplate.opsForValue().get(dailyKey(userId, purpose));
        return val == null ? 0 : Integer.parseInt(val);
    }

    @Override
    public int incrementDailySendCount(UUID userId, OtpToken.Purpose purpose) {
        String key = dailyKey(userId, purpose);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) count = 0L;
        redisTemplate.expire(key, Duration.ofSeconds(secondsUntilMidnight()));
        return count.intValue();
    }

    @Override
    public void blockAccount(UUID userId) {
        redisTemplate.opsForValue().set(blockedKey(userId), "1");
    }

    @Override
    public boolean isAccountBlocked(UUID userId) {
        Boolean exists = redisTemplate.hasKey(blockedKey(userId));
        return exists != null && exists;
    }

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Helper Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    private long secondsUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, midnight).getSeconds();
    }
}
