package com.ecommerce.inventory.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.inventory.infrastructure.persistence.mapper.InventoryDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryItemRepositoryImpl implements InventoryItemRepository {

    private final InventoryItemJpaRepository jpaRepository;
    private final InventoryDomainMapper mapper;

    @Override
    public Optional<InventoryItem> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId).map(mapper::toDomain);
    }

    @Override
    public Optional<InventoryItem> findByProductIdAndVariantId(UUID productId, UUID variantId) {
        return jpaRepository.findByProductIdAndVariantId(productId, variantId).map(mapper::toDomain);
    }

    @Override
    public Optional<InventoryItem> findForUpdate(UUID productId) {
        return jpaRepository.findByProductIdWithLock(productId).map(mapper::toDomain);
    }

    @Override
    public Optional<InventoryItem> findForUpdate(UUID productId, UUID variantId) {
        return jpaRepository.findByProductIdAndVariantIdWithLock(productId, variantId).map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findAllByProductId(UUID productId) {
        return jpaRepository.findAllByProductId(productId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<InventoryItem> findAllBySellerId(UUID sellerId) {
        return jpaRepository.findAllBySellerId(sellerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<InventoryItem> findAllBySellerId(UUID sellerId, Pageable pageable) {
        return jpaRepository.findAllBySellerId(sellerId, pageable).map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpaRepository.findAllActive().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<InventoryItem> findAll(Pageable pageable) {
        return jpaRepository.findAllActive(pageable).map(mapper::toDomain);
    }

    @Override
    public long countLowStock() {
        return jpaRepository.countLowStock();
    }

    @Override
    public void save(InventoryItem inventoryItem) {
        jpaRepository.save(mapper.toJpa(inventoryItem));
    }

    @Override
    public void saveAll(List<InventoryItem> inventoryItems) {
        jpaRepository.saveAll(inventoryItems.stream().map(mapper::toJpa).toList());
    }

    @Override
    public List<InventoryItem> findAllByProductIdIn(List<UUID> productIds) {
        return jpaRepository.findAllByProductIdIn(productIds).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
