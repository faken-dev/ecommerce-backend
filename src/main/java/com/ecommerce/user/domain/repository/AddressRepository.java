package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.entity.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository {
    Address save(Address address);
    Optional<Address> findById(UUID id);
    List<Address> findByUserIdOrderByDefault(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
    Optional<Address> findDefaultByUserId(UUID userId);
    void unmarkAllDefaultsForUser(UUID userId, UUID excludeId);
    long countByUserId(UUID userId);
    void delete(Address address);

    /** Cascade-delete all addresses on user soft-deletion. */
    void deleteAllByUserId(UUID userId);
}
