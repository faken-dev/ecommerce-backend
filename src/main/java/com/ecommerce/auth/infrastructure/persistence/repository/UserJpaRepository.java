package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // Fetch roles + permissions trong 1 query - tránh N+1 problem
    @Query("""
            SELECT u FROM UserJpaEntity u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.email = :email
              AND u.deletedAt IS NULL
            """)
    Optional<UserJpaEntity> findByEmailWithRoles(String email);

    @Query("""
            SELECT u FROM UserJpaEntity u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :id
              AND u.deletedAt IS NULL
            """)
    Optional<UserJpaEntity> findByIdWithRoles(UUID id);

    Optional<UserJpaEntity> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    // Fetch roles + permissions cho findByPhoneNumber - tránh N+1
    @Query("""
            SELECT u FROM UserJpaEntity u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.phoneNumber = :phoneNumber
              AND u.deletedAt IS NULL
            """)
    Optional<UserJpaEntity> findByPhoneNumberWithRoles(String phoneNumber);

    /**
     * Finds a user by OAuth provider + provider's subject ID.
     * Used for linking an existing OAuth account on repeated logins.
     */
    Optional<UserJpaEntity> findByProviderAndProviderUserId(
            OAuth2Provider provider, String providerUserId);

    @Query("""
            SELECT u FROM UserJpaEntity u
            LEFT JOIN u.roles r
            WHERE (:role IS NULL OR r.name = :role)
              AND (:search IS NULL OR :search = '' 
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND u.deletedAt IS NULL
            """)
    Page<UserJpaEntity> searchUsers(
            String role, String search, Pageable pageable);
}
