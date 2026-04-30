package com.ecommerce.catalog.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "catalog_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductJpaEntity extends AuditableJpaEntity {

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(nullable = false, unique = true, length = 350)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 19, scale = 4)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_per_item", precision = 19, scale = 4)
    private BigDecimal costPerItem;

    @Column(length = 100)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(columnDefinition = "text")
    private String tags;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private boolean isFeatured = false;

    @Column(nullable = false, length = 20)
    private String visibility;

    @Column(name = "meta_title", length = 300)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Column(name = "weight_kg", precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "three_d_model_url", columnDefinition = "TEXT")
    private String threeDModelUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImageJpaEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductVariantJpaEntity> variants = new ArrayList<>();

    public void addImage(ProductImageJpaEntity image) {
        images.add(image);
        image.setProduct(this);
    }

    public void addVariant(ProductVariantJpaEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
}
