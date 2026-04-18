package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ProductImage extends AuditableEntity {

    private UUID productId;
    private String url;
    private String altText;
    private int sortOrder;
    private Integer width;
    private Integer height;
    private Integer fileSizeKb;
    private boolean primary;

    public static ProductImageBuilder create(UUID productId, String url, String altText) {
        return ProductImage.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .url(url)
                .altText(altText)
                .sortOrder(0)
                .primary(false);
    }

    public void markAsPrimary() {
        this.primary = true;
        this.setUpdateAt(Instant.now());
    }

    public void updateOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        this.setUpdateAt(Instant.now());
    }

    @Builder
    public ProductImage(UUID id, UUID productId, String url, String altText,
            int sortOrder, Integer width, Integer height, Integer fileSizeKb,
            boolean primary, Instant createdAt, Instant updatedAt,
            UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.productId = productId;
        this.url = url;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.width = width;
        this.height = height;
        this.fileSizeKb = fileSizeKb;
        this.primary = primary;
    }
}