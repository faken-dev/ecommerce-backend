package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    Optional<Role> findById(UUID id);
    Optional<Role> findByIdWithPermissions(UUID id);
    List<Role> findAll();
    Role save(Role role);
    void deleteById(UUID id, UUID deletedBy);
}
