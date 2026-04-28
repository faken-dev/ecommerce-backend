package com.ecommerce.auth.infrastructure.persistence.entity;

import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_otp_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OtpTokenJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpToken.Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpToken.Purpose purpose;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(nullable = false)
    @Builder.Default
    private int attemptCount = 0;
}
