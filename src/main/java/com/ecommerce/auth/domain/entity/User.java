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
    private String profilePictureUrl;
    private HashedPassword passwordHash;
    private boolean active;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean otpBlocked;
    private Instant otpBlockedAt;
    private String otpBlockedReason;
    private OAuth2Provider provider;
    private String providerUserId;
    private Set<Role> roles;
    private Instant deletedAt;
    private UUID deletedBy;

    public static User create(Email email, HashedPassword passwordHash, String fullName) {
        return User.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .email(email)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(new HashSet<>())
                .build();
    }

    public static User createOAuth(OAuth2Provider provider, String providerUserId,
                                            Email email, String fullName, String avatarUrl) {
        return User.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .passwordHash(null)
                .fullName(fullName)
                .profilePictureUrl(avatarUrl)
                .active(true)
                .emailVerified(true)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(new HashSet<>())
                .build();
    }

    public RegistrationCompletedEvent toRegistrationCompletedEvent() {
        return new RegistrationCompletedEvent(getId(), email.value(), fullName, Instant.now());
    }

    public EmailVerifiedEvent toEmailVerifiedEvent() {
        return new EmailVerifiedEvent(getId(), email.value(), fullName, Instant.now());
    }

    public PasswordChangedEvent toPasswordChangedEvent() {
        return new PasswordChangedEvent(getId(), email.value(), fullName, Instant.now());
    }

    public record DeletedEvent(UUID userId, Instant occurredAt) {}

    public boolean isOAuthUser() {
        return provider != null && provider.isOAuth();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void ensureActive() {
        if (isDeleted()) throw new BusinessException(ErrorCode.AUTH_USER_DELETED);
        if (!active)    throw new BusinessException(ErrorCode.AUTH_ACCOUNT_BLOCKED);
    }

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
        this.touchUpdate();
    }

    public DeletedEvent softDeleteAndReturnEvent(UUID deletedBy) {
        softDelete(deletedBy);
        return new DeletedEvent(getId(), deletedAt);
    }

    public void blockAccount() {
        this.active = false;
        this.touchUpdate();
    }

    public void unblockAccount() {
        this.active = true;
        this.touchUpdate();
    }

    public void blockOtp(String reason) {
        this.otpBlocked = true;
        this.otpBlockedAt = Instant.now();
        this.otpBlockedReason = reason;
        this.touchUpdate();
    }

    public void unblockOtp() {
        this.otpBlocked = false;
        this.otpBlockedAt = null;
        this.otpBlockedReason = null;
        this.touchUpdate();
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
        this.touchUpdate();
    }

    public void updateProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
        this.touchUpdate();
    }
}
