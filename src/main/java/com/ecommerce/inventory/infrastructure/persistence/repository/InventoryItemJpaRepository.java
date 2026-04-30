package com.ecommerce.inventory.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID> {
    
    Optional<InventoryItemJpaEntity> findByProductId(UUID productId);

    Optional<InventoryItemJpaEntity> findByProductIdAndVariantId(UUID productId, UUID variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemJpaEntity i WHERE i.productId = :productId AND i.variantId IS NULL")
    Optional<InventoryItemJpaEntity> findByProductIdWithLock(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemJpaEntity i WHERE i.productId = :productId AND i.variantId = :variantId")
    Optional<InventoryItemJpaEntity> findByProductIdAndVariantIdWithLock(UUID productId, UUID variantId);

    List<InventoryItemJpaEntity> findAllByProductId(UUID productId);
    
    @Query("SELECT COUNT(i) FROM InventoryItemJpaEntity i WHERE (i.quantity - i.reservedQuantity) < i.lowStockThreshold")
    long countLowStock();

    @Query(value = "SELECT i.* FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.seller_id = :sellerId AND p.deleted_at IS NULL", 
           countQuery = "SELECT COUNT(i.id) FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.seller_id = :sellerId AND p.deleted_at IS NULL",
           nativeQuery = true)
    Page<InventoryItemJpaEntity> findAllBySellerId(UUID sellerId, Pageable pageable);

    @Query(value = "SELECT i.* FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.seller_id = :sellerId AND p.deleted_at IS NULL", nativeQuery = true)
    List<InventoryItemJpaEntity> findAllBySellerId(UUID sellerId);

    @Query(value = "SELECT i.* FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.deleted_at IS NULL", 
           countQuery = "SELECT COUNT(i.id) FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.deleted_at IS NULL",
           nativeQuery = true)
    Page<InventoryItemJpaEntity> findAllActive(Pageable pageable);

    @Query(value = "SELECT i.* FROM inventory_items i JOIN catalog_products p ON i.product_id = p.id WHERE p.deleted_at IS NULL", nativeQuery = true)
    List<InventoryItemJpaEntity> findAllActive();

    List<InventoryItemJpaEntity> findAllByProductIdIn(List<UUID> productIds);
}
