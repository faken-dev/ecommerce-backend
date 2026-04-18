package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenJpaEntity r WHERE r.tokenHash = :tokenHash")
    Optional<RefreshTokenJpaEntity> findByTokenHashForUpdate(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshTokenJpaEntity r
            SET r.revokedAt = :now
            WHERE r.userId = :userId
              AND r.revokedAt IS NULL
            """)
    void revokeAllByUserId(UUID userId, Instant now);

    @Modifying
    @Query("""
            DELETE FROM RefreshTokenJpaEntity r
            WHERE r.expiresAt < :now
              OR r.revokedAt IS NOT NULL
            """)
    int deleteExpiredAndRevoked(Instant now);
}