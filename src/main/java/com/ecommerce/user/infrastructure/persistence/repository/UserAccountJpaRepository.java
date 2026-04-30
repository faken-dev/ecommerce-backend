package com.ecommerce.user.infrastructure.persistence.repository;

import com.ecommerce.user.infrastructure.persistence.entity.UserAccountJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
    
    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM UserAccountJpaEntity u
        LEFT JOIN FETCH u.roles
        WHERE u.id = :id
        """)
    Optional<UserAccountJpaEntity> findByIdWithRoles(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT u FROM UserAccountJpaEntity u
        LEFT JOIN u.roles r
        WHERE u.deletedAt IS NULL
        AND (:role IS NULL OR r.name = :role)
        AND (:search IS NULL OR LOWER(CAST(u.fullName AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CAST(u.email AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<UserAccountJpaEntity> searchUsers(@Param("role") String role, 
                                          @Param("search") String search, 
                                          Pageable pageable);
}
