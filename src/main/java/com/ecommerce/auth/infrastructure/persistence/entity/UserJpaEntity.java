package com.ecommerce.auth.infrastructure.persistence.entity;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for the {@code auth_users} table.
 *
 * <p>Uses {@link Getter}/{@link Setter} (Lombok) for simplicity and consistency
 * with other JPA entities in this project. Restricting JPA entity access via
 * visibility modifiers is an anti-pattern — the repository is the only caller
 * in practice.
 *
 * <p>The {@link #copyScalarFieldsFrom(User)} method provides a single, controlled
 * update path from the domain entity that explicitly preserves the {@code roles}
 * collection. Callers must <strong>not</strong> replace the roles reference.
 */
@Entity
@Table(name = "auth_users")
@Getter
@Setter
public class UserJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(length = 255)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "is_otp_blocked", nullable = false)
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

    // ── Controlled Mutation API ────────────────────────────────────────────────

    /**
     * Copies all scalar and value-object fields from the domain {@link User}
     * into this JPA entity, <strong>without replacing the {@code roles} collection</strong>.
     *
     * This is the preferred update path from {@link UserRepositoryImpl}.
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
        // NOTE: roles is intentionally NOT copied — the managed collection reference
        // must be preserved. Callers must not reassign this field.
    }

    /**
     * Adds a role to this user entity.
     * Preferred over direct {@code getRoles().add(...)} because it is self-documenting
     * and is the designated entry point for role management on the JPA entity.
     */
    public void addRole(RoleJpaEntity role) {
        this.roles.add(role);
    }
}
