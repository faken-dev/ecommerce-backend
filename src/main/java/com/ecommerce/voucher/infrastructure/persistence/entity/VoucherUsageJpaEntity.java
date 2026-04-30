package com.ecommerce.voucher.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voucher_usages")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class VoucherUsageJpaEntity extends AuditableJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherJpaEntity voucher;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "discount_applied", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountApplied;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt;

    public VoucherJpaEntity getVoucher() { return voucher; }
    public void setVoucher(VoucherJpaEntity voucher) { this.voucher = voucher; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
}
