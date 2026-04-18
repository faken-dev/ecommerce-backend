package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository jpaRoleRepository;
    private final AuthDomainMapper mapper;

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRoleRepository.findByName(name)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toRole);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRoleRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toRole);
    }

    @Override
    public Optional<Role> findByIdWithPermissions(UUID id) {
        return jpaRoleRepository.findByIdWithPermissions(id)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toRole);
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAllActiveWithPermissions().stream()
                .map(mapper::toRole)
                .toList();
    }

    @Override
    @Transactional
    public Role save(Role role) {
        var saved = jpaRoleRepository.save(mapper.toJpa(role));
        return mapper.toRole(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID deletedBy) {
        jpaRoleRepository.findById(id).ifPresent(jpa -> {
            jpa.setDeletedAt(Instant.now());
            jpa.setUpdatedBy(deletedBy);
            jpaRoleRepository.save(jpa);
        });
    }
}