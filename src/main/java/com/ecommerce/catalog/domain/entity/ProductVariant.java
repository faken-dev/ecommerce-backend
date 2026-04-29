package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductVariant extends AuditableEntity {
    private UUID productId;
    private String sku;
    private BigDecimal price;
    private int stockQuantity;
    private String option1Name;
    private String option1Value;
    private String option2Name;
    private String option2Value;
    private String imageUrl;
    private boolean active;

    public boolean isAvailable() {
        return active && stockQuantity > 0;
    }
}