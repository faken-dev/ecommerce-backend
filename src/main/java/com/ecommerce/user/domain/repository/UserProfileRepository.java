package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.entity.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    UserProfile save(UserProfile userProfile);
    Optional<UserProfile> findById(UUID id);
    Optional<UserProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    /** Cascade-delete on user soft-deletion. */
    void deleteByUserId(UUID userId);
}
