package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.infrastructure.persistence.entity.OtpTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenJpaRepository extends JpaRepository<OtpTokenJpaEntity, UUID> {

    
    
    @Query("""
            SELECT o FROM OtpTokenJpaEntity o
            WHERE o.userId = :userId
              AND o.purpose = :purpose
              AND o.usedAt IS NULL
              AND o.expiresAt > :now
            ORDER BY o.createdAt DESC
            LIMIT 1
            """)
    Optional<OtpTokenJpaEntity> findLatestActive(
            UUID userId, OtpToken.Purpose purpose, Instant now);

    @Modifying
    @Query("DELETE FROM OtpTokenJpaEntity o WHERE o.expiresAt < :now")
    void deleteExpiredTokens(Instant now);

    @Query("""
        SELECT COUNT(o) FROM OtpTokenJpaEntity o
        WHERE o.userId = :userId
          AND o.purpose = :purpose
        """)
        int countByUserIdAndPurpose(UUID userId, OtpToken.Purpose purpose);

        @Query("""
                SELECT o FROM OtpTokenJpaEntity o
                WHERE o.userId = :userId
                AND o.purpose = :purpose
                ORDER BY o.createdAt DESC
                LIMIT 1
                """)
        Optional<OtpTokenJpaEntity> findLatestByUserIdAndPurpose(
                UUID userId, OtpToken.Purpose purpose);
        }
