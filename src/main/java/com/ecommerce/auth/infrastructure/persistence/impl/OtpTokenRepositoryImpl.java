package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.OtpTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OtpTokenRepositoryImpl implements OtpTokenRepository {

    private final OtpTokenJpaRepository jpaRepository;
    private final AuthDomainMapper mapper;
    private final StringRedisTemplate redisTemplate;  // ← Redis cho raw OTP cache

    private static final String RAW_OTP_PREFIX = "otp:raw:";

    @Override
    public OtpToken save(OtpToken token) {
        return mapper.toOtpToken(
                jpaRepository.save(mapper.toJpa(token)));
    }

    @Override
    public Optional<OtpToken> findLatestActiveByUserIdAndPurpose(
            UUID userId, OtpToken.Purpose purpose) {
        return jpaRepository
                .findLatestActive(userId, purpose, Instant.now())
                .map(mapper::toOtpToken);
    }

    @Override
    public void deleteExpiredTokens(Instant cutoff) {
        jpaRepository.deleteExpiredTokens(cutoff);
    }

    @Override
    public int countByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose) {
        return jpaRepository.countByUserIdAndPurpose(userId, purpose);
    }

    @Override
    public Optional<OtpToken> findLatestByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose) {
        return jpaRepository
                .findLatestByUserIdAndPurpose(userId, purpose)
                .map(mapper::toOtpToken);
    }

    // ── Raw OTP (Redis) ────────────────────────────────────────────────

    @Override
    public void storeRawOtp(UUID userId, OtpToken.Purpose purpose, String rawOtp, long ttlSeconds) {
        String key = rawOtpKey(userId, purpose);
        redisTemplate.opsForValue().set(key, rawOtp, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<String> getRawOtp(UUID userId, OtpToken.Purpose purpose) {
        String key = rawOtpKey(userId, purpose);
        String rawOtp = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(rawOtp);
    }

    private String rawOtpKey(UUID userId, OtpToken.Purpose purpose) {
        return RAW_OTP_PREFIX + purpose.name() + ":" + userId;
    }
}
