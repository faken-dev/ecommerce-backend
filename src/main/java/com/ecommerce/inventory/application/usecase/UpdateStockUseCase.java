package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.entity.InventoryLog;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.inventory.domain.repository.InventoryLogRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UpdateStockUseCase {
    private static final Logger log = LoggerFactory.getLogger(UpdateStockUseCase.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Transactional
    public void execute(UUID productId, UUID variantId, int change, String actionType, String reason) {
        log.info("Adjusting stock for product {} (variant={}): change={}, type={}, reason={}", 
                productId, variantId, change, actionType, reason);

        // Fetch or create the inventory item
        InventoryItem inventoryItem = findOrCreateInventoryItem(productId, variantId);

        int previousStock = inventoryItem.getQuantity();
        inventoryItem.adjustQuantity(change);
        int newStock = inventoryItem.getQuantity();

        if (newStock < 0 && change < 0) {
            throw new RuntimeException("Insufficient stock for product " + productId + (variantId != null ? " variant " + variantId : ""));
        }

        inventoryItemRepository.save(inventoryItem);
        
        // Persist InventoryLog
        InventoryLog inventoryLog = InventoryLog.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .changeAmount(change)
                .stockAfter(newStock)
                .actionType(actionType)
                .reason(reason)
                .build();
        
        inventoryLogRepository.save(inventoryLog);
        log.info("Stock adjusted for product {} (variant={}). Previous stock: {}, New stock: {}", 
                productId, variantId, previousStock, newStock);
    }

    private InventoryItem findOrCreateInventoryItem(UUID productId, UUID variantId) {
        var existing = variantId == null 
                ? inventoryItemRepository.findForUpdate(productId)
                : inventoryItemRepository.findForUpdate(productId, variantId);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        log.info("Inventory item not found for product {} (variant={}). Creating new record.", productId, variantId);
        return InventoryItem.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .variantId(variantId)
                .quantity(0)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .build();
    }
}
