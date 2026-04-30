package com.ecommerce.inventory.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryItem extends AuditableEntity {
    private UUID slotId;
    private UUID productId;
    private UUID variantId;
    private int quantity;
    private int reservedQuantity;
    private int lowStockThreshold;
    private Long version;

    public void reserve(int amount) {
        if (getAvailableQuantity() < amount) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Insufficient available stock to reserve");
        }
        this.reservedQuantity += amount;
    }

    public void release(int amount) {
        if (this.reservedQuantity < amount) {
            this.reservedQuantity = 0;
        } else {
            this.reservedQuantity -= amount;
        }
    }

    public void deductFromReserved(int amount) {
        if (this.quantity < amount) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Insufficient total stock to deduct");
        }
        this.quantity -= amount;
        this.release(amount);
    }

    public int getAvailableQuantity() {
        return Math.max(0, quantity - reservedQuantity);
    }

    public void adjustQuantity(int adjustment) {
        this.quantity = Math.max(0, this.quantity + adjustment);
    }
}
