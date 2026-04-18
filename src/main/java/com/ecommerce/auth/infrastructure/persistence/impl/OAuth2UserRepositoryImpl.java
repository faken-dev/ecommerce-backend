package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.application.port.OAuth2UserRepository;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.UserJpaRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implements OAuth user find-or-create with thread-safety under concurrent first-time logins.
 *
 * Handles two distinct constraint violations:
 * <ol>
 *   <li>{@code (provider, provider_user_id)} — concurrent race, retry yields the existing user</li>
 *   <li>{@code email} — email already registered via email/password, cannot link to OAuth</li>
 * </ol>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserRepositoryImpl implements OAuth2UserRepository {

    private final UserJpaRepository jpaRepository;
    private final RoleRepository roleRepository;
    private final AuthDomainMapper mapper;

    @Override
    public User findOrCreate(OAuth2Provider provider, String providerUserId,
                           Email email, String fullName, String avatarUrl) {
        // Fast path: existing user
        Optional<User> existing = jpaRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(mapper::toUser);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Slow path: create new user with BUYER role
        User newUser = buildOAuthUser(provider, providerUserId, email, fullName);

        try {
            UserJpaEntity savedJpa = jpaRepository.save(mapper.toJpa(newUser));
            log.info("Created new OAuth user: provider={}, email={}, userId={}",
                    provider, email.value(), savedJpa.getId());
            return mapper.toUser(savedJpa);

        } catch (DataIntegrityViolationException e) {
            // Distinguish: email collision vs. concurrent provider+providerUserId race
            if (isEmailConstraintViolation(e)) {
                log.warn("OAuth login rejected — email already registered via email/password: {}",
                        email.value());
                throw new BusinessException(
                        ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                        "This email is already registered with a password. "
                                + "Please log in with your email and password, then link your "
                                + provider + " account in your profile settings.");
            }

            // Concurrent race: another thread created the user first — retry the lookup
            log.debug("Concurrent OAuth user creation detected, retrying lookup: provider={}, providerUserId={}",
                    provider, providerUserId);
            return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                    .map(mapper::toUser)
                    .orElseThrow(() -> new IllegalStateException(
                            "OAuth user not found after constraint violation — check unique constraints"));
        }
    }

    /**
     * Returns true if the exception is caused by the email unique constraint
     * rather than the (provider, providerUserId) unique constraint.
     */
    private boolean isEmailConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            String constraintName = cve.getConstraintName();
            if (constraintName != null && constraintName.toLowerCase().contains("email")) {
                return true;
            }
        }
        // Fallback: check the SQLSTATE in the message or nested cause
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return message.contains("email") && message.contains("unique");
    }

    private User buildOAuthUser(OAuth2Provider provider, String providerUserId,
                                Email email, String fullName) {
        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseThrow(() -> new IllegalStateException(
                        "BUYER role not found — run V1 migration"));

        User user = User.createOAuth(provider, providerUserId, email, fullName, null)
                .build();
        user.addRole(buyerRole);
        return user;
    }
}
