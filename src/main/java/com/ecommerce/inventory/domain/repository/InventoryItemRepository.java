package com.ecommerce.inventory.domain.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import com.ecommerce.inventory.domain.entity.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository {
    Optional<InventoryItem> findByProductId(UUID productId);
    Optional<InventoryItem> findByProductIdAndVariantId(UUID productId, UUID variantId);
    Optional<InventoryItem> findForUpdate(UUID productId);
    Optional<InventoryItem> findForUpdate(UUID productId, UUID variantId);
    List<InventoryItem> findAllByProductId(UUID productId);
    List<InventoryItem> findAllBySellerId(UUID sellerId);
    Page<InventoryItem> findAllBySellerId(UUID sellerId, Pageable pageable);
    List<InventoryItem> findAll();
    Page<InventoryItem> findAll(Pageable pageable);
    long countLowStock();
    void save(InventoryItem inventoryItem);
    void saveAll(List<InventoryItem> inventoryItems);
    List<InventoryItem> findAllByProductIdIn(List<UUID> productIds);
}
