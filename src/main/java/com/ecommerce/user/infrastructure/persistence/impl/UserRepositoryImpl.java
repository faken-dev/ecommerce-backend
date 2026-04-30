package com.ecommerce.user.infrastructure.persistence.impl;

import com.ecommerce.user.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.ecommerce.user.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.ecommerce.user.infrastructure.persistence.repository.UserAccountJpaRepository;
import com.ecommerce.user.infrastructure.persistence.repository.UserRoleAccountJpaRepository;
import com.ecommerce.user.application.mapper.UserMapper;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository("userModuleUserRepository")
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserAccountJpaRepository jpaRepository;
    private final UserRoleAccountJpaRepository roleJpaRepository;
    private final UserMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findByIdWithRoles(id).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserAccountJpaEntity entity = jpaRepository.findById(user.getId())
                .orElse(new UserAccountJpaEntity());
        
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setFullName(user.getFullName());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setProfilePictureUrl(user.getProfilePictureUrl());
        entity.setActive(user.isActive());
        entity.setEmailVerified(user.isEmailVerified());
        
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<UserRoleJpaEntity> roleEntities = roleJpaRepository.findByNameIn(user.getRoles());
            if (roleEntities.size() != user.getRoles().size()) {
                Set<String> foundNames = roleEntities.stream().map(UserRoleJpaEntity::getName).collect(Collectors.toSet());
                String missing = user.getRoles().stream()
                        .filter(name -> !foundNames.contains(name))
                        .collect(Collectors.joining(", "));
                throw new RuntimeException("Roles not found: " + missing);
            }
            entity.setRoles(new HashSet<>(roleEntities));
        }

        UserAccountJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<User> searchUsers(String role, String search, Pageable pageable) {
        return jpaRepository.searchUsers(role, search, pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
