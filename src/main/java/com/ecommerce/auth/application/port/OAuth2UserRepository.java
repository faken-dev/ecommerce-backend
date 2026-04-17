package com.ecommerce.auth.application.port;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;

/**
 * Repository port for OAuth user loading/creation.
 *
 * Thread-safety: if two concurrent OAuth logins arrive for the same Google account
 * before either inserts, the second insert fails on unique-constraint violation.
 * The calling code retries by re-querying the existing user.
 */
public interface OAuth2UserRepository {

    /**
     * Finds an existing OAuth user, or creates a new one with the BUYER role.
     * OAuth users have no password — {@code passwordHash} is null.
     *
     * @param provider        the identity provider (GOOGLE, APPLE, etc.)
     * @param providerUserId  the provider's unique subject ID (Google "sub" claim)
     * @param email           user's email address
     * @param fullName        user's display name
     * @param avatarUrl       profile picture URL (may be null)
     * @return the existing or newly created user
     */
    User findOrCreate(OAuth2Provider provider, String providerUserId,
                      Email email, String fullName, String avatarUrl);
}
