package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class InventoryLog extends AuditableEntity {
    private UUID productId;
    private String actionType; // IN, OUT, ADJUST
    private int quantity;
    private int previousStock;
    private int newStock;
    private String reason;
}
