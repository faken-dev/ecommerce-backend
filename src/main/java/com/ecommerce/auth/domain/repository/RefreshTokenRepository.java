package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);
    void revokeAllByUserId(UUID userId);

    int deleteExpiredAndRevoked(Instant now);
}
