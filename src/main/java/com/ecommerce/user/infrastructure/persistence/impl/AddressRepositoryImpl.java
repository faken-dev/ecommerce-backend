package com.ecommerce.user.infrastructure.persistence.impl;

import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.infrastructure.persistence.entity.AddressJpaEntity;
import com.ecommerce.user.infrastructure.persistence.mapper.UserDomainMapper;
import com.ecommerce.user.infrastructure.persistence.repository.AddressJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository jpaRepository;
    private final UserDomainMapper mapper;

    @Override
    public Address save(Address address) {
        AddressJpaEntity jpa = jpaRepository.findById(address.getId())
                .map(existing -> mapper.toJpa(address))
                .orElseGet(() -> mapper.toJpa(address));

        return mapper.toDomain(jpaRepository.save(jpa));
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Address> findByUserIdOrderByDefault(UUID userId) {
        return jpaRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Address> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Address> findDefaultByUserId(UUID userId) {
        return jpaRepository.findDefaultByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public void unmarkAllDefaultsForUser(UUID userId, UUID excludeId) {
        jpaRepository.unmarkAllDefaultsForUser(userId, excludeId);
    }

    @Override
    public long countByUserId(UUID userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public void delete(Address address) {
        jpaRepository.delete(mapper.toJpa(address));
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        jpaRepository.deleteAllByUserId(userId);
    }
}