package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class RefreshToken extends AuditableEntity {

    private final UUID userId;
    /** SHA-256 hash of the raw refresh token. */
    private final String tokenHash;
    private final String deviceInfo;
    private final String ipAddress;
    private final Instant expiresAt;
    private Instant revokedAt;

    /**
     * Generation within the token family.
     * Incremented each time the user successfully refreshes.
     * Used to detect replay attacks: if token.generation &lt; currentFamilyGeneration → attack.
     */
    private long generation;



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

    /** Reconstitutes from the database with existing audit fields preserved. */
    public static RefreshToken reconstitute(UUID id, Instant createdAt, Instant updatedAt,
                                           UUID createdBy, UUID updatedBy,
                                           UUID userId, String tokenHash,
                                           String deviceInfo, String ipAddress,
                                           Instant expiresAt, Instant revokedAt,
                                           long generation) {
        return RefreshToken.builder()
                .id(id).createdAt(createdAt).updatedAt(updatedAt)
                .createdBy(createdBy).updatedBy(updatedBy)
                .userId(userId).tokenHash(tokenHash)
                .deviceInfo(deviceInfo).ipAddress(ipAddress)
                .expiresAt(expiresAt).revokedAt(revokedAt)
                .generation(generation)
                .build();
    }

    @Builder
    public RefreshToken(UUID id, String tokenHash, UUID userId,
                        String deviceInfo, String ipAddress,
                        Instant expiresAt, Instant revokedAt,
                        Instant createdAt, Instant updatedAt,
                        UUID createdBy, UUID updatedBy,
                        long generation) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.revokedAt = revokedAt;
        this.generation = generation;
    }

    // ── Domain Rules ──────────────────────────────────────────────────────────

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isRevoked() { return revokedAt != null; }
    public boolean isValid()   { return !isRevoked() && !isExpired(); }

    /** Throws if revoked or expired. */
    public void ensureValid() {
        if (isRevoked()) throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
        if (isExpired()) throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
    }

    /**
     * Revokes this token, recording the ID of the token that replaces it
     * (for rotation tracking).
     */
    public void revoke(UUID replacedByTokenId) {
        if (!isRevoked()) {
            this.revokedAt = Instant.now();
            this.setUpdateAt(Instant.now());
            this.setUpdatedBy(replacedByTokenId);
        }
    }

    /** Revokes without tracking replacement (logout / system revocation). */
    public void revoke() {
        if (!isRevoked()) {
            this.revokedAt = Instant.now();
            this.setUpdateAt(Instant.now());
            // SYSTEM_ACTOR is the sentinel UUID for automated/system operations
            this.setUpdatedBy(SYSTEM_ACTOR);
        }
    }

    /** Sentinel UUID for system/automated operations (matches JpaConfig.UUID0). */
    public static final UUID SYSTEM_ACTOR =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    /** Sentinel UUID for theft-detection events. */
    public static final UUID THEFT_DETECTOR =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
}
