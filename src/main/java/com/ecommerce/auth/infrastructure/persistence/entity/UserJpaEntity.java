package com.ecommerce.auth.infrastructure.persistence.entity;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for the {@code auth_users} table.
 */
@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(name = "is_otp_blocked", nullable = false)
    @Builder.Default
    private boolean otpBlocked = false;

    @Column(name = "otp_blocked_at")
    private Instant otpBlockedAt;

    @Column(name = "otp_blocked_reason", length = 255)
    private String otpBlockedReason;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "auth_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleJpaEntity> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OAuth2Provider provider;

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Column
    private Instant deletedAt;

    @Column(length = 255)
    private UUID deletedBy;

    // - Controlled Mutation API ------

    /**
     * Copies all scalar and value-object fields from the domain {@link User}
     * into this JPA entity, <strong>without replacing the {@code roles} collection</strong>.
     */
    public void copyScalarFieldsFrom(User user) {
        this.email             = user.getEmail().value();
        this.fullName          = user.getFullName();
        this.active            = user.isActive();
        this.emailVerified     = user.isEmailVerified();
        this.phoneVerified     = user.isPhoneVerified();
        this.otpBlocked        = user.isOtpBlocked();
        this.otpBlockedAt      = user.getOtpBlockedAt();
        this.otpBlockedReason  = user.getOtpBlockedReason();
        this.deletedAt         = user.getDeletedAt();
        this.deletedBy         = user.getDeletedBy();
        if (user.getPasswordHash() != null) {
            this.passwordHash = user.getPasswordHash().value();
        }
        this.provider          = user.getProvider();
        this.providerUserId    = user.getProviderUserId();
    }

    /**
     * Adds a role to this user entity.
     */
    public void addRole(RoleJpaEntity role) {
        this.roles.add(role);
    }
}
