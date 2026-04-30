package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProductLocationUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public void execute(UUID productId, UUID slotId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Inventory item not found"));
        
        // In a real system, we'd also check slot capacity here
        inventoryItemRepository.save(InventoryItem.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .slotId(slotId)
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .createdAt(item.getCreatedAt())
                .createdBy(item.getCreatedBy())
                .version(item.getVersion())
                .build());
    }
}
