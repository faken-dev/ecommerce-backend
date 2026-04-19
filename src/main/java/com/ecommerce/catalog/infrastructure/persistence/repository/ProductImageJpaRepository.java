package com.ecommerce.catalog.infrastructure.persistence.repository;

import com.ecommerce.catalog.infrastructure.persistence.entity.ProductImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, UUID> {
    List<ProductImageJpaEntity> findByProductIdOrderBySortOrderAsc(UUID productId);
    Optional<ProductImageJpaEntity> findByProductIdAndPrimaryTrue(UUID productId);
    void deleteByProductId(UUID productId);
}