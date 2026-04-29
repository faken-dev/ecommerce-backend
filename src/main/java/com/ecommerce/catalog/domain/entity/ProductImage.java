package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductImage extends AuditableEntity {
    private UUID productId;
    private String url;
    private String altText;
    private int sortOrder;
    private boolean primary;

    public static ProductImage create(UUID productId, String url, String altText) {
        return ProductImage.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .url(url)
                .altText(altText)
                .sortOrder(0)
                .primary(false)
                .build();
    }

    public void markAsPrimary() {
        this.primary = true;
        this.setUpdatedAt(Instant.now());
    }

    public void updateOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        this.setUpdatedAt(Instant.now());
    }
}