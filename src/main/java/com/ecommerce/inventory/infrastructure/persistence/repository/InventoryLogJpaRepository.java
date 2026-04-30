package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryLogJpaRepository extends JpaRepository<InventoryLogJpaEntity, UUID> {
    List<InventoryLogJpaEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
