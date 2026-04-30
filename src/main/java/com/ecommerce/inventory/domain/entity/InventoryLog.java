package com.ecommerce.inventory.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class InventoryLog extends AuditableEntity {
    private UUID productId;
    private int changeAmount;
    private int stockAfter;
    private String actionType; // RESTOCK, ADJUST, ORDER_DEDUCTION, RETURN
    private String reason;
    private UUID operatorId;

    public InventoryLog() {}

    public static InventoryLogBuilder builder() {
        return new InventoryLogBuilder();
    }

    public static class InventoryLogBuilder {
        private final InventoryLog log = new InventoryLog();

        public InventoryLogBuilder id(UUID id) { log.setId(id); return this; }
        public InventoryLogBuilder productId(UUID productId) { log.productId = productId; return this; }
        public InventoryLogBuilder changeAmount(int changeAmount) { log.changeAmount = changeAmount; return this; }
        public InventoryLogBuilder stockAfter(int stockAfter) { log.stockAfter = stockAfter; return this; }
        public InventoryLogBuilder actionType(String actionType) { log.actionType = actionType; return this; }
        public InventoryLogBuilder reason(String reason) { log.reason = reason; return this; }
        public InventoryLogBuilder operatorId(UUID operatorId) { log.operatorId = operatorId; return this; }
        public InventoryLogBuilder createdAt(Instant createdAt) { log.setCreatedAt(createdAt); return this; }
        public InventoryLogBuilder updatedAt(Instant updatedAt) { log.setUpdatedAt(updatedAt); return this; }

        public InventoryLog build() {
            return log;
        }
    }

    public UUID getProductId() { return productId; }
    public int getChangeAmount() { return changeAmount; }
    public int getStockAfter() { return stockAfter; }
    public String getActionType() { return actionType; }
    public String getReason() { return reason; }
    public UUID getOperatorId() { return operatorId; }
}
