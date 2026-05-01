package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RefreshToken extends AuditableEntity {

    private UUID userId;
    /** SHA-256 hash of the raw refresh token. */
    private String tokenHash;
    private String deviceInfo;
    private String ipAddress;
    private Instant expiresAt;
    private Instant revokedAt;

    /**
     * Generation within the token family.
     * Incremented each time the user successfully refreshes.
     */
    private long generation;

    /** Sentinel UUID for system/automated operations (matches JpaConfig.UUID0). */
    public static final UUID SYSTEM_ACTOR =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    /** Sentinel UUID for theft-detection events. */
    public static final UUID THEFT_DETECTOR =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    /** Creates a new refresh token. Generation is set by TokenService. */
    public static RefreshToken create(UUID userId, String tokenHash,
                                     String deviceInfo, String ipAddress,
                                     long expirationMs, long generation) {
        return RefreshToken.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .tokenHash(tokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusMillis(expirationMs))
                .generation(generation)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isRevoked() { return revokedAt != null; }
    public boolean isValid()   { return !isRevoked() && !isExpired(); }

    /** Throws if revoked (and outside grace period) or expired. */
    public void ensureValid(long gracePeriodSeconds) {
        if (isRevoked()) {
            Instant graceThreshold = revokedAt.plusSeconds(gracePeriodSeconds);
            if (Instant.now().isAfter(graceThreshold)) {
                throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
            }
        }
        if (isExpired()) throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
    }

    public void ensureValid() {
        ensureValid(0);
    }

    /** Revokes this token. */
    public void revoke(UUID replacedByTokenId) {
        if (!isRevoked()) {
            this.revokedAt = Instant.now();
            this.touchUpdate();
            this.setUpdatedBy(replacedByTokenId);
        }
    }

    /** Revokes without tracking replacement. */
    public void revoke() {
        if (!isRevoked()) {
            this.revokedAt = Instant.now();
            this.touchUpdate();
            this.setUpdatedBy(SYSTEM_ACTOR);
        }
    }
}
