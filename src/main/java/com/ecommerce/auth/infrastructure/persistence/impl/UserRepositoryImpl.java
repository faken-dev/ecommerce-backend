package com.ecommerce.auth.infrastructure.persistence.impl;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.mapper.AuthDomainMapper;
import com.ecommerce.auth.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final AuthDomainMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpa = jpaRepository.findById(user.getId())
                .map(existing -> {
                    existing.copyScalarFieldsFrom(user);
                    // Explicitly merge roles added via user.addRole() since
                    // copyScalarFieldsFrom() intentionally skips the managed collection.
                    existing.getRoles().clear();
                    user.getRoles().forEach(domainRole ->
                            existing.addRole(mapper.toJpa(domainRole)));
                    return existing;
                })
                .orElseGet(() -> {
                    UserJpaEntity newJpa = mapper.toJpa(user);
                    // mapper.toJpa ignores roles; wire them in for a new user.
                    user.getRoles().forEach(domainRole ->
                            newJpa.addRole(mapper.toJpa(domainRole)));
                    return newJpa;
                });

        return mapper.toUser(jpaRepository.save(jpa));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findByIdWithRoles(id)
                .map(mapper::toUser);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmailWithRoles(email.value())
                .map(mapper::toUser);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return jpaRepository.findByPhoneNumberAndDeletedAtIsNull(phoneNumber)
                .map(mapper::toUser);
    }

    @Override
    public Optional<User> findByPhoneNumberWithRoles(String phoneNumber) {
        return jpaRepository.findByPhoneNumberWithRoles(phoneNumber)
                .map(mapper::toUser);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return jpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public void updateFullName(UUID userId, String fullName) {
        jpaRepository.findById(userId).ifPresent(jpa -> {
            jpa.setFullName(fullName);
            jpaRepository.save(jpa);
        });
    }
}