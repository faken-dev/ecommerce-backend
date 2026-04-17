package com.ecommerce.auth.infrastructure.persistence.mapper;

import com.ecommerce.auth.domain.entity.*;
import com.ecommerce.auth.infrastructure.persistence.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthDomainMapper {

    // ── User ──────────────────────────────────────────────
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", expression = "java(new com.ecommerce.auth.domain.valueobject.Email(jpa.getEmail()))")
    @Mapping(target = "phoneNumber", expression = "java(jpa.getPhoneNumber() != null ? new com.ecommerce.auth.domain.valueobject.PhoneNumber(jpa.getPhoneNumber()) : null)")
    @Mapping(target = "passwordHash", expression = "java(jpa.getPasswordHash() != null ? com.ecommerce.auth.domain.valueobject.HashedPassword.of(jpa.getPasswordHash()) : null)")
    @Mapping(target = "provider", source = "jpa.provider")
    @Mapping(target = "providerUserId", source = "jpa.providerUserId")
    User toUser(UserJpaEntity jpa);

    /**
     * Maps User → UserJpaEntity for INSERT (new user).
     *
     * Does NOT map the `roles` field — roles are a managed JPA collection that
     * must NOT be replaced on update. Callers must preserve the existing
     * {@code roles} reference when merging an update.
     *
     * For updates, prefer {@link #toJpaPreservingRoles(User, UserJpaEntity)} or
     * merge manually and only copy scalar fields.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", expression = "java(user.getEmail().value())")
    @Mapping(target = "phoneNumber", expression = "java(user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null)")
    @Mapping(target = "passwordHash", expression = "java(user.getPasswordHash() != null ? user.getPasswordHash().value() : null)")
    @Mapping(target = "provider", source = "user.provider")
    @Mapping(target = "providerUserId", source = "user.providerUserId")
    @Mapping(target = "roles", ignore = true)
    UserJpaEntity toJpa(User user);

    // ── Role & Permission ─────────────────────────────────
    @Mapping(target = "id", source = "id")
    Role toRole(RoleJpaEntity jpa);

    @Mapping(target = "id", source = "id")
    RoleJpaEntity toJpa(Role role);

    @Mapping(target = "id", source = "id")
    Permission toPermission(PermissionJpaEntity jpa);

    @Mapping(target = "id", source = "id")
    PermissionJpaEntity toJpa(Permission permission);

    // ── OtpToken ──────────────────────────────────────────
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codeHash", source = "codeHash")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "purpose", source = "purpose")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "usedAt", source = "usedAt")
    @Mapping(target = "attemptCount", source = "attemptCount")
    OtpToken toOtpToken(OtpTokenJpaEntity jpa);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codeHash", source = "codeHash")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "purpose", source = "purpose")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "usedAt", source = "usedAt")
    @Mapping(target = "attemptCount", source = "attemptCount")
    OtpTokenJpaEntity toJpa(OtpToken token);

    // ── RefreshToken ──────────────────────────────────────
    @Mapping(target = "id", source = "id")
    @Mapping(target = "generation", source = "generation")
    RefreshToken toRefreshToken(RefreshTokenJpaEntity jpa);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "generation", source = "generation")
    RefreshTokenJpaEntity toJpa(RefreshToken token);
}