package com.ecommerce.auth.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RefreshTokenJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(length = 500)
    private String deviceInfo;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    /**
     * Generation within the token family.
     * Starts at 1. Incremented on each successful refresh.
     */
    @Column(nullable = false)
    @Builder.Default
    private long generation = 1;
}
