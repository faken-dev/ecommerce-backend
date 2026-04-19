package com.ecommerce.catalog.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "catalog_product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariantJpaEntity extends AuditableJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(length = 100)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 19, scale = 4)
    private BigDecimal compareAtPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Column(name = "option2_name", length = 50)
    private String option2Name;

    @Column(name = "option2_value", length = 100)
    private String option2Value;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}