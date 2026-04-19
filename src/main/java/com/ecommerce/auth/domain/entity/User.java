package com.ecommerce.auth.domain.entity;

import com.ecommerce.auth.domain.event.EmailVerifiedEvent;
import com.ecommerce.auth.domain.event.PasswordChangedEvent;
import com.ecommerce.auth.domain.event.RegistrationCompletedEvent;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.auth.domain.valueobject.PhoneNumber;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class User extends AuditableEntity {

    private Email email;
    private PhoneNumber phoneNumber;
    private String fullName;
    private HashedPassword passwordHash;
    /** false = blocked, true = active. Soft-deleted accounts have deletedAt set instead. */
    private boolean active;
    private boolean emailVerified;
    private boolean phoneVerified;
    /** OTP sending blocked for this user (spam/abuse). */
    private boolean otpBlocked;
    private Instant otpBlockedAt;
    private String otpBlockedReason;

    /** Identity provider: EMAIL, GOOGLE. Null means standard email/password login. */
    private OAuth2Provider provider;
    /** Provider's subject ID (Google "sub" claim). Set when provider != null. */
    private String providerUserId;

    private Set<Role> roles;

    // Soft deletion
    private Instant deletedAt;
    private UUID deletedBy;

    /** Creates a new user — ID is auto-generated as time-ordered UUID. */
    public static UserBuilder create(Email email, HashedPassword passwordHash, String fullName) {
        return User.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .email(email)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(new HashSet<>());
    }

    /** Creates a new OAuth user — no password, email is pre-verified by the provider. */
    public static UserBuilder createOAuth(OAuth2Provider provider, String providerUserId,
                                          Email email, String fullName, String avatarUrl) {
        return User.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .passwordHash(null)
                .fullName(fullName)
                .active(true)
                .emailVerified(true)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(new HashSet<>());
    }

    /** Reconstitutes a user from the database — preserves existing ID. */
    public static UserBuilder reconstitute() {
        return User.builder();
    }

    @Builder
    public User(UUID id, Email email, PhoneNumber phoneNumber, String fullName,
                HashedPassword passwordHash, boolean active, boolean emailVerified,
                boolean phoneVerified, boolean otpBlocked, Instant otpBlockedAt,
                String otpBlockedReason, OAuth2Provider provider, String providerUserId,
                Set<Role> roles,
                Instant deletedAt, UUID deletedBy,
                Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.active = active;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.otpBlocked = otpBlocked;
        this.otpBlockedAt = otpBlockedAt;
        this.otpBlockedReason = otpBlockedReason;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.roles = roles != null ? roles : new HashSet<>();
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    // ─── Domain Event Factories ────────────────────────────────────────────────
    // Convention: entity NEVER directly publishes events.
    // Application layer calls EventPublisher.publish(entity.to*Event()) after save.
    // Only add a factory method when the entity owns ALL data needed for the event.
    // Events that need cross-boundary data or timestamps generated at call-site
    // should be constructed directly in the application layer.

    /** Event published after registration is persisted. */
    public RegistrationCompletedEvent toRegistrationCompletedEvent() {
        return new RegistrationCompletedEvent(getId(), email.value(), fullName, Instant.now());
    }

    /** Event published after email verification succeeds. */
    public EmailVerifiedEvent toEmailVerifiedEvent() {
        return new EmailVerifiedEvent(getId(), email.value(), fullName, Instant.now());
    }

    /** Event published after password change. */
    public PasswordChangedEvent toPasswordChangedEvent() {
        return new PasswordChangedEvent(getId(), email.value(), fullName, Instant.now());
    }

    /**
     * Domain event published when the user soft-deletes their account.
     * Handlers in the user module cascade-delete UserProfile and Address rows.
     */
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
        this.setUpdateAt(Instant.now());
    }

    /**
     * Soft-deletes and returns a domain event for the caller to publish.
     * Domain entities must NOT publish events directly — that is the application layer's job.
     * The returned DeletedEvent must be published via EventPublisher after this method.
     */
    public DeletedEvent softDeleteAndReturnEvent(UUID deletedBy) {
        softDelete(deletedBy);
        return new DeletedEvent(this.getId(), Instant.now());
    }

    public void blockAccount() {
        this.active = false;
        this.setUpdateAt(Instant.now());
    }

    public void unblockAccount() {
        this.active = true;
        this.setUpdateAt(Instant.now());
    }

    public void blockOtp(String reason) {
        this.otpBlocked = true;
        this.otpBlockedAt = Instant.now();
        this.otpBlockedReason = reason;
        this.setUpdateAt(Instant.now());
    }

    public void unblockOtp() {
        this.otpBlocked = false;
        this.otpBlockedAt = null;
        this.otpBlockedReason = null;
        this.setUpdateAt(Instant.now());
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

    /**
     * Updates display name. Called when UserProfile fullName is changed —
     * keeps the auth layer's fullName in sync with the user profile.
     */
    public void updateFullName(String fullName) {
        this.fullName = fullName;
        this.setUpdateAt(Instant.now());
    }

}