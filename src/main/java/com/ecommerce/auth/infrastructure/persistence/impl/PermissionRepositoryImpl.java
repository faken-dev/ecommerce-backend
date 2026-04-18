package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.domain.entity.Permission;
import com.ecommerce.auth.domain.repository.PermissionRepository;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final AuthDomainMapper mapper;

    @Override
    public Permission save(Permission permission) {
        var jpa = mapper.toJpa(permission);
        var saved = jpaRepository.save(jpa);
        return mapper.toPermission(saved);
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toPermission);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toPermission);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAllActive().stream()
                .map(mapper::toPermission)
                .toList();
    }

    @Override
    public List<Permission> findByIdIn(List<UUID> ids) {
        return jpaRepository.findByIdIn(ids).stream()
                .map(mapper::toPermission)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.findById(id).ifPresent(jpa -> {
            jpa.setDeletedAt(Instant.now());
            jpaRepository.save(jpa);
        });
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
