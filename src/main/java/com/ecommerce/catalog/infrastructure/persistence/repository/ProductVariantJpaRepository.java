package com.ecommerce.catalog.infrastructure.persistence.repository;

import com.ecommerce.catalog.infrastructure.persistence.entity.ProductVariantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, UUID> {
    List<ProductVariantJpaEntity> findByProductIdOrderBySortOrderAsc(UUID productId);
    void deleteByProductId(UUID productId);
}