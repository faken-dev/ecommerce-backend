package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
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
    private String barcode;
    private String title;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private int stockQuantity;
    private String optionName;
    private String optionValue;
    private String option2Name;
    private String option2Value;
    private String imageUrl;
    private boolean active;

    public static ProductVariant create(UUID productId, String title,
            String optionName, String optionValue) {
        return ProductVariant.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .title(title)
                .optionName(optionName)
                .optionValue(optionValue)
                .stockQuantity(0)
                .active(true)
                .build();
    }

    public void updatePricing(BigDecimal price, BigDecimal compareAtPrice) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.touchUpdate();
    }

    public void adjustStock(int quantity) {
        this.stockQuantity = Math.max(0, this.stockQuantity + quantity);
        this.touchUpdate();
    }

    public void activate() {
        this.active = true;
        this.touchUpdate();
    }

    public void deactivate() {
        this.active = false;
        this.touchUpdate();
    }
}
