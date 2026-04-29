package com.ecommerce.auth.domain.entity;

import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.auth.domain.valueobject.PhoneNumber;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class User extends AuditableEntity {

    private Email email;
    private PhoneNumber phoneNumber;
    private String fullName;
    private HashedPassword passwordHash;
    @SuperBuilder.Default
    private boolean active = true;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean otpBlocked;
    private Instant otpBlockedAt;
    private String otpBlockedReason;

    // OAuth2 support
    private OAuth2Provider provider;
    private String providerUserId;
    private String profilePictureUrl;

    @SuperBuilder.Default
    private Set<Role> roles = new HashSet<>();

    private Instant deletedAt;
    private UUID deletedBy;

    // Domain event inner class
    public record DeletedEvent(UUID userId, Instant occurredAt) {}

    // ── Domain Rules ──────────────────────────────────────────────────────────

    /** True if this user authenticated via an OAuth provider (Google, Apple, etc.). */
    public boolean isOAuthUser() {
        return provider != null && provider.isOAuth();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /** Throws if account is deleted or blocked. */
    public void ensureActive() {
        if (isDeleted()) throw new BusinessException(ErrorCode.AUTH_USER_DELETED);
        if (!active)    throw new BusinessException(ErrorCode.AUTH_ACCOUNT_BLOCKED);
    }

    /** Throws if OTP sending is blocked for this user. */
    public void ensureOtpNotBlocked() {
        if (otpBlocked) throw new BusinessException(ErrorCode.AUTH_OTP_BLOCKED);
    }

    public void verifyEmail()   { this.emailVerified = true; }
    public void verifyPhone()   { this.phoneVerified = true; }
    public void changePassword(HashedPassword newHash) { this.passwordHash = newHash; }

    public void softDelete(UUID deletedBy) {
        if (isDeleted()) return;
        this.active = false;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.setUpdatedAt(Instant.now());
    }

    public DeletedEvent softDeleteAndReturnEvent(UUID deletedBy) {
        softDelete(deletedBy);
        return new DeletedEvent(this.getId(), Instant.now());
    }

    public void blockAccount() {
        this.active = false;
        this.setUpdatedAt(Instant.now());
    }

    public void unblockAccount() {
        this.active = true;
        this.setUpdatedAt(Instant.now());
    }

    public void blockOtp(String reason) {
        this.otpBlocked = true;
        this.otpBlockedAt = Instant.now();
        this.otpBlockedReason = reason;
        this.setUpdatedAt(Instant.now());
    }

    public void unblockOtp() {
        this.otpBlocked = false;
        this.otpBlockedAt = null;
        this.otpBlockedReason = null;
        this.setUpdatedAt(Instant.now());
    }

    public void addRole(Role role) { this.roles.add(role); }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }

    public Set<String> getAllPermissions() {
        return roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    public void updateFullName(String fullName) {
        this.fullName = fullName;
        this.setUpdatedAt(Instant.now());
    }
}