package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.OtpToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository {

    OtpToken save(OtpToken token);

    Optional<OtpToken> findLatestActiveByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose);

    void deleteExpiredTokens(Instant cutoff);

    int countByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose);

    Optional<OtpToken> findLatestByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose);

    void storeRawOtp(UUID userId, OtpToken.Purpose purpose, String rawOtp, long ttlSeconds);

    Optional<String> getRawOtp(UUID userId, OtpToken.Purpose purpose);
}
