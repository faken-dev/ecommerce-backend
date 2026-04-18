package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the RefreshToken repository.
 *
 * <p>Class-level {@code @Transactional} ensures every method runs in a transaction.
 * Read-only methods are explicitly annotated with {@code readOnly = true} for
 * performance and consistency guarantees.
 */
@Repository
@RequiredArgsConstructor
@Transactional
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final AuthDomainMapper mapper;

    @Override
    public RefreshToken save(RefreshToken token) {
        return mapper.toRefreshToken(
                jpaRepository.save(mapper.toJpa(token)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
                .map(mapper::toRefreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        return jpaRepository.findByTokenHashForUpdate(tokenHash)
                .map(mapper::toRefreshToken);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Override
    public int deleteExpiredAndRevoked(Instant now) {
        return jpaRepository.deleteExpiredAndRevoked(now);
    }
}