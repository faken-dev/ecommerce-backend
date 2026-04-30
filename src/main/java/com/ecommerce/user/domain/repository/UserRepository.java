package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    User save(User user);
    Page<User> findAll(Pageable pageable);
    Page<User> searchUsers(String role, String search, Pageable pageable);
    void deleteById(UUID id);
}
