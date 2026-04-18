package com.ecommerce.user.infrastructure.persistence.repository;

import com.ecommerce.user.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {

    Optional<UserProfileJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    /** Cascade-delete on user soft-deletion. */
    void deleteByUserId(UUID userId);
}
