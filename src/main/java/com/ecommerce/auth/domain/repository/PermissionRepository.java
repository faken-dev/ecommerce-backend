package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    Permission save(Permission permission);
    Optional<Permission> findById(UUID id);
    Optional<Permission> findByName(String name);
    List<Permission> findAll();
    List<Permission> findByIdIn(List<UUID> ids);
    void deleteById(UUID id);
    boolean existsByName(String name);
}
