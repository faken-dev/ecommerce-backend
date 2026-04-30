package com.ecommerce.inventory.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "inventory_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryLogJpaEntity extends AuditableJpaEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "change_amount", nullable = false)
    private int changeAmount;

    @Column(name = "stock_after", nullable = false)
    private int stockAfter;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    private String reason;

    @Column(name = "operator_id")
    private UUID operatorId;
    
    @Version
    private Long version;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public int getChangeAmount() { return changeAmount; }
    public void setChangeAmount(int changeAmount) { this.changeAmount = changeAmount; }
    public int getStockAfter() { return stockAfter; }
    public void setStockAfter(int stockAfter) { this.stockAfter = stockAfter; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getOperatorId() { return operatorId; }
    public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
