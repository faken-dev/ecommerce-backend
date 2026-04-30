package com.ecommerce.inventory.infrastructure.adapter;

import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.order.application.port.InventoryQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryQueryAdapter implements InventoryQueryPort {

    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public int getAvailableStock(UUID productId, UUID variantId) {
        if (variantId == null) {
            return inventoryItemRepository.findByProductId(productId)
                    .map(InventoryItem::getAvailableQuantity)
                    .orElse(0);
        }
        return inventoryItemRepository.findByProductIdAndVariantId(productId, variantId)
                .map(InventoryItem::getAvailableQuantity)
                .orElse(0);
    }

    @Override
    public long countLowStock() {
        return inventoryItemRepository.countLowStock();
    }
}
