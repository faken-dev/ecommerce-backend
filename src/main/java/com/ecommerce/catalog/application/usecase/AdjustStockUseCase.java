package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.AdjustStockCommand;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdjustStockUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public void execute(AdjustStockCommand command) {
        InventoryItem inventoryItem = inventoryItemRepository.findByProductId(command.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
                        "No inventory found for product: " + command.productId()));

        inventoryItem.adjustQuantity(command.adjustment());
        inventoryItemRepository.save(inventoryItem);
    }
}
