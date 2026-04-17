package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumberWithRoles(String phoneNumber);  // ← FETCH JOIN, tránh N+1
    boolean existsByEmail(Email email);
    boolean existsByPhoneNumber(String phoneNumber);

    void updateFullName(UUID userId, String fullName);
}