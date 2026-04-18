package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.infrastructure.persistence.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {
    Optional<PermissionJpaEntity> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT p FROM PermissionJpaEntity p WHERE p.id IN :ids")
    List<PermissionJpaEntity> findByIdIn(List<UUID> ids);

    @Query("SELECT p FROM PermissionJpaEntity p WHERE p.deletedAt IS NULL")
    List<PermissionJpaEntity> findAllActive();
}
