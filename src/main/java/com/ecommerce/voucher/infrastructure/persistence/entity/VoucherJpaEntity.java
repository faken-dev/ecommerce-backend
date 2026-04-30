package com.ecommerce.voucher.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "voucher_vouchers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VoucherJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String scope = "GLOBAL";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 19, scale = 4)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "max_usage_total", nullable = false)
    @Builder.Default
    private int maxUsageTotal = -1;

    @Column(name = "max_usage_per_user", nullable = false)
    @Builder.Default
    private int maxUsagePerUser = 1;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "applicable_product_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> applicableProductIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "applicable_category_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> applicableCategoryIds = new ArrayList<>();

    @Column(name = "seller_id")
    private UUID sellerId;

    @Column(name = "current_usage_count", nullable = false)
    @Builder.Default
    private int currentUsageCount = 0;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "requires_collection", nullable = false)
    @Builder.Default
    private boolean requiresCollection = false;


    // - Helpers --------------

    /**
     * Maps PostgreSQL ARRAY columns to Set<UUID> for the domain entity.
     */
    public Set<UUID> getApplicableProductIdsAsSet() {
        return applicableProductIds != null
                ? new HashSet<>(applicableProductIds)
                : new HashSet<>();
    }

    public Set<UUID> getApplicableCategoryIdsAsSet() {
        return applicableCategoryIds != null
                ? new HashSet<>(applicableCategoryIds)
                : new HashSet<>();
    }
}
