package com.ecommerce.catalog.domain.entity;

import com.ecommerce.catalog.domain.event.ProductActivatedEvent;
import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.domain.event.ProductDeletedEvent;
import com.ecommerce.catalog.domain.event.ProductUpdateEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Product — catalog aggregate root.
 * Sellers create/manage products; buyers browse/search.
 */
@Getter
public class Product extends AuditableEntity {

    public enum Status { DRAFT, ACTIVE, ARCHIVED, DELETED }
    public enum Visibility { PUBLIC, SHOP, HIDDEN }

    private UUID sellerId;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal costPerItem;
    private int stockQuantity;
    private int lowStockThreshold;
    private String sku;
    private String barcode;
    private UUID categoryId;
    private List<String> tags;
    private Status status;
    private boolean isFeatured;
    private Visibility visibility;
    private String metaTitle;
    private String metaDescription;
    private BigDecimal averageRating;
    private int reviewCount;
    private BigDecimal weightKg;
    private String weightUnit;
    private Instant deletedAt;

    // ── Factory ────────────────────────────────────────────────────────────────

    public static ProductBuilder create(UUID sellerId, String name, String slug,
                                        String description, BigDecimal price) {
        validateSlug(slug);
        validatePrice(price);
        return Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .sellerId(sellerId)
                .name(name.trim())
                .slug(slug.toLowerCase().trim())
                .description(description != null ? description.trim() : null)
                .price(price)
                .stockQuantity(0)
                .lowStockThreshold(10)
                .status(Status.ACTIVE)
                .isFeatured(false)
                .visibility(Visibility.SHOP)
                .reviewCount(0)
                .weightUnit("KG");
    }

    public static ProductBuilder createAsDraft(UUID sellerId, String name, String slug,
                                               BigDecimal price) {
        validateSlug(slug);
        validatePrice(price);
        return Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .sellerId(sellerId)
                .name(name.trim())
                .slug(slug.toLowerCase().trim())
                .price(price)
                .stockQuantity(0)
                .lowStockThreshold(10)
                .status(Status.DRAFT)
                .isFeatured(false)
                .visibility(Visibility.SHOP)
                .reviewCount(0)
                .weightUnit("KG");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private static void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Slug is required");
        }
        String n = slug.toLowerCase().trim();
        if (n.length() > 350) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Slug must not exceed 350 characters");
        }
        if (!n.matches("[a-z0-9]+(-[a-z0-9]+)*")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Slug must be lowercase alphanumeric, hyphenated (e.g. iphone-15-pro)");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Price must be zero or positive");
        }
    }

    // ── Domain Methods ───────────────────────────────────────────────────────

    public void update(String name, String description, BigDecimal price,
                       BigDecimal compareAtPrice, UUID categoryId, List<String> tags,
                       String metaTitle, String metaDescription) {
        if (name != null) this.name = name.trim();
        if (description != null) this.description = description.trim();
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        this.compareAtPrice = compareAtPrice;
        this.categoryId = categoryId;
        this.tags = tags;
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.setUpdateAt(Instant.now());
    }

    public void updatePricing(BigDecimal price, BigDecimal compareAtPrice) {
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        this.compareAtPrice = compareAtPrice;
        this.setUpdateAt(Instant.now());
    }

    public void adjustStock(int adjustment) {
        this.stockQuantity = Math.max(0, this.stockQuantity + adjustment);
        this.setUpdateAt(Instant.now());
    }

    public void setStockQuantity(int quantity) {
        if (quantity < 0) throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                "Stock cannot be negative");
        this.stockQuantity = quantity;
        this.setUpdateAt(Instant.now());
    }

    public void activate() {
        if (this.status == Status.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ACTIVE,
                    "Cannot activate a deleted product");
        }
        this.status = Status.ACTIVE;
        this.setUpdateAt(Instant.now());
    }

    public void archive() {
        this.status = Status.ARCHIVED;
        this.setUpdateAt(Instant.now());
    }

    public void softDelete() {
        this.status = Status.DELETED;
        this.deletedAt = Instant.now();
        this.setUpdateAt(Instant.now());
    }

    public void restore() {
        this.status = Status.DRAFT;
        this.deletedAt = null;
        this.setUpdateAt(Instant.now());
    }

    public boolean isAvailable() {
        return status == Status.ACTIVE && deletedAt == null;
    }

    public boolean isLowOnStock() {
        return stockQuantity <= lowStockThreshold;
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    // ── Events ───────────────────────────────────────────────────────────────

    public ProductCreatedEvent toCreatedEvent() {
        return new ProductCreatedEvent(getId(), sellerId, name, price, Instant.now());
    }

    public ProductUpdateEvent toUpdatedEvent() {
        return new ProductUpdateEvent(getId(), sellerId, name, price, Instant.now());
    }

    public ProductActivatedEvent toActivatedEvent() {
        return new ProductActivatedEvent(getId(), Instant.now());
    }

    public ProductDeletedEvent toDeletedEvent() {
        return new ProductDeletedEvent(getId(), Instant.now());
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    @Builder
    public Product(UUID id, UUID sellerId, String name, String slug, String description,
                   BigDecimal price, BigDecimal compareAtPrice, BigDecimal costPerItem,
                   int stockQuantity, int lowStockThreshold, String sku, String barcode,
                   UUID categoryId, List<String> tags, Status status, boolean isFeatured,
                   Visibility visibility, String metaTitle, String metaDescription,
                   BigDecimal averageRating, int reviewCount, BigDecimal weightKg,
                   String weightUnit, Instant deletedAt,
                   Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.sellerId = sellerId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.costPerItem = costPerItem;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.sku = sku;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.tags = tags;
        this.status = status;
        this.isFeatured = isFeatured;
        this.visibility = visibility;
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.weightKg = weightKg;
        this.weightUnit = weightUnit != null ? weightUnit : "KG";
        this.deletedAt = deletedAt;
    }
}