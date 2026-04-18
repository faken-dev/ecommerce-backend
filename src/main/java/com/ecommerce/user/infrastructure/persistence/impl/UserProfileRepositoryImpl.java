package com.ecommerce.user.infrastructure.persistence.impl;

import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.user.infrastructure.persistence.entity.UserProfileJpaEntity;
import com.ecommerce.user.infrastructure.persistence.mapper.UserDomainMapper;
import com.ecommerce.user.infrastructure.persistence.repository.UserProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileJpaRepository jpaRepository;
    private final UserDomainMapper mapper;

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileJpaEntity jpa = jpaRepository.findById(userProfile.getId())
                .map(existing -> mapper.toJpa(userProfile))
                .orElseGet(() -> mapper.toJpa(userProfile));

        return mapper.toDomain(jpaRepository.save(jpa));
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}