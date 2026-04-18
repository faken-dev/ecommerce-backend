package com.ecommerce.user.infrastructure.persistence.repository;

import com.ecommerce.user.infrastructure.persistence.entity.AddressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {



    Optional<AddressJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT a FROM AddressJpaEntity a WHERE a.userId = :userId ORDER BY a.defaultAddress DESC, a.createdAt DESC")    
    List<AddressJpaEntity> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(UUID userId);

    Optional<AddressJpaEntity> findDefaultByUserId(UUID userId);

    @Modifying
    @Query("UPDATE AddressJpaEntity a SET a.defaultAddress = false WHERE a.userId = :userId AND a.id <> :excludeId")
    void unmarkAllDefaultsForUser(@Param("userId") UUID userId, @Param("excludeId") UUID excludeId);

    long countByUserId(UUID userId);

    /** Cascade-delete all addresses on user soft-deletion. */
    void deleteAllByUserId(UUID userId);
}
