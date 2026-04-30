package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumberWithRoles(String phoneNumber);
    boolean existsByEmail(Email email);
    boolean existsByPhoneNumber(String phoneNumber);

    void updateFullName(UUID userId, String fullName);
    void updateAvatar(UUID userId, String avatarUrl);
}