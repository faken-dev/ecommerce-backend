package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
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

    public static ProductVariantBuilder create(UUID productId, String title,
            String optionName, String optionValue) {
        return ProductVariant.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .title(title)
                .optionName(optionName)
                .optionValue(optionValue)
                .stockQuantity(0)
                .active(true);
    }

    public void updatePricing(BigDecimal price, BigDecimal compareAtPrice) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.setUpdateAt(Instant.now());
    }

    public void adjustStock(int quantity) {
        this.stockQuantity = Math.max(0, this.stockQuantity + quantity);
        this.setUpdateAt(Instant.now());
    }

    public void activate() {
        this.active = true;
        this.setUpdateAt(Instant.now());
    }

    public void deactivate() {
        this.active = false;
        this.setUpdateAt(Instant.now());
    }

    @Builder
    public ProductVariant(UUID id, UUID productId, String sku, String barcode,
            String title, BigDecimal price, BigDecimal compareAtPrice,
            int stockQuantity, String optionName, String optionValue,
            String option2Name, String option2Value, String imageUrl, boolean active,
            Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.productId = productId;
        this.sku = sku;
        this.barcode = barcode;
        this.title = title;
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.stockQuantity = stockQuantity;
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.option2Name = option2Name;
        this.option2Value = option2Value;
        this.imageUrl = imageUrl;
        this.active = active;
    }
}