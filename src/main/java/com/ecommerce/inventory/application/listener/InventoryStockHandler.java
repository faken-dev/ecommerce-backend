package com.ecommerce.inventory.application.listener;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.order.domain.event.OrderCancelledEvent;
import com.ecommerce.order.domain.event.OrderConfirmedEvent;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class InventoryStockHandler {
    private static final Logger log = LoggerFactory.getLogger(InventoryStockHandler.class);

    private final InventoryItemRepository inventoryItemRepository;

    @EventListener
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Initializing inventory for new product [productId={}]", event.productId());
        
        // Simple products (no variants) or default stock entry
        InventoryItem item = InventoryItem.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(event.productId())
                .variantId(null)
                .quantity(0)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .build();
        
        inventoryItemRepository.save(item);
    }

    @EventListener
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Reserving stock for new order [orderId={}]", event.orderId());
        
        for (var item : event.items()) {
            InventoryItem inventoryItem = findInventory(item.productId(), item.variantId())
                    .orElseThrow(() -> {
                        log.error("Inventory item not found for product: {} variant: {}", item.productId(), item.variantId());
                        return new BusinessException(ErrorCode.VALIDATION_FAILED, "Inventory item not found");
                    });

            log.info("Reserving {} units for product {}", item.quantity(), item.productId());
            inventoryItem.reserve(item.quantity());
            inventoryItemRepository.save(inventoryItem);
        }
    }

    @EventListener
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Deducting stock for confirmed order [orderId={}]", event.orderId());

        for (var item : event.items()) {
            InventoryItem inventoryItem = findInventory(item.productId(), item.variantId())
                    .orElseThrow(() -> {
                        log.error("Inventory item not found for product: {} variant: {}", item.productId(), item.variantId());
                        return new BusinessException(ErrorCode.VALIDATION_FAILED, "Inventory item not found");
                    });

            log.info("Deducting {} units from inventory for product {}", item.quantity(), item.productId());
            inventoryItem.deductFromReserved(item.quantity());
            inventoryItemRepository.save(inventoryItem);
        }
    }

    @EventListener
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Releasing reserved stock for cancelled order [orderId={}]", event.orderId());
        
        for (var item : event.items()) {
            InventoryItem inventoryItem = findInventory(item.productId(), item.variantId())
                    .orElseThrow(() -> {
                        log.error("Inventory item not found for product: {} variant: {}", item.productId(), item.variantId());
                        return new BusinessException(ErrorCode.VALIDATION_FAILED, "Inventory item not found");
                    });

            log.info("Releasing {} reserved units for product {}", item.quantity(), item.productId());
            inventoryItem.release(item.quantity());
            inventoryItemRepository.save(inventoryItem);
        }
    }

    private Optional<InventoryItem> findInventory(UUID productId, UUID variantId) {
        if (variantId == null) {
            return inventoryItemRepository.findByProductId(productId);
        }
        return inventoryItemRepository.findByProductIdAndVariantId(productId, variantId);
    }
}
