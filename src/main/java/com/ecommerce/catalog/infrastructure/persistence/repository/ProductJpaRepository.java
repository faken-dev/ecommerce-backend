package com.ecommerce.catalog.infrastructure.persistence.repository;

import com.ecommerce.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findByIdAndDeletedAtIsNull(UUID id);
    Optional<ProductJpaEntity> findBySlugAndDeletedAtIsNull(String slug);

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.sellerId = :sellerId
          AND p.slug = :slug
          AND p.deletedAt IS NULL
        """)
    Optional<ProductJpaEntity> findActiveBySellerIdAndSlug(
            @Param("sellerId") UUID sellerId,
            @Param("slug") String slug);

    boolean existsBySellerIdAndSlug(UUID sellerId, String slug);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ProductJpaEntity p
        WHERE p.sellerId = :sellerId
          AND p.slug = :slug
          AND p.id != :excludeId
          AND p.deletedAt IS NULL
        """)
    boolean existsBySellerIdAndSlugExcludingId(
            @Param("sellerId") UUID sellerId,
            @Param("slug") String slug,
            @Param("excludeId") UUID excludeId);

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.deletedAt IS NULL
          AND p.status = 'ACTIVE'
          AND (COALESCE(:categoryIds, NULL) IS NULL OR p.categoryId IN :categoryIds)
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY p.createdAt DESC
        """)
    Page<ProductJpaEntity> search(
            @Param("q") String query,
            @Param("categoryIds") Collection<UUID> categoryIds,
            Pageable pageable);

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.categoryId = :categoryId
          AND p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    Page<ProductJpaEntity> findByCategory(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.sellerId = :sellerId
          AND p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    Page<ProductJpaEntity> findBySeller(@Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.status = 'ACTIVE'
          AND p.visibility IN ('PUBLIC', 'SHOP')
          AND p.deletedAt IS NULL
        ORDER BY p.isFeatured DESC, p.createdAt DESC
        """)
    Page<ProductJpaEntity> findActivePublic(Pageable pageable);

    long countBySellerIdAndDeletedAtIsNull(UUID sellerId);
    Page<ProductJpaEntity> findAllByDeletedAtIsNull(Pageable pageable);
    @EntityGraph(attributePaths = {"images"})
    List<ProductJpaEntity> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}
