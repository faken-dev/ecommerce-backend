package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByName(String name);

    List<RoleJpaEntity> findByNameIn(Collection<String> names);

    @Query("""
        SELECT r FROM RoleJpaEntity r
        LEFT JOIN FETCH r.permissions
        WHERE r.name = :name
        """)
    Optional<RoleJpaEntity> findByNameWithPermissions(String name);

    @Query("""
        SELECT r FROM RoleJpaEntity r
        LEFT JOIN FETCH r.permissions
        WHERE r.id = :id
        """)
    Optional<RoleJpaEntity> findByIdWithPermissions(UUID id);

    @Query("SELECT r FROM RoleJpaEntity r LEFT JOIN FETCH r.permissions WHERE r.deletedAt IS NULL")
    List<RoleJpaEntity> findAllActiveWithPermissions();
}
