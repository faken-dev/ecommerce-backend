package com.ecommerce.voucher.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
public class VoucherUsage extends AuditableEntity {

    private UUID voucherId;
    private UUID userId;
    private UUID orderId;
    private BigDecimal discountApplied;
    private Instant usedAt;

    public static VoucherUsageBuilder<?, ?> record(
            UUID voucherId,
            UUID userId,
            UUID orderId,
            BigDecimal discountApplied) {

        return VoucherUsage.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .voucherId(voucherId)
                .userId(userId)
                .orderId(orderId)
                .discountApplied(discountApplied)
                .usedAt(Instant.now());
    }

    public UUID getVoucherId() { return voucherId; }
    public UUID getUserId() { return userId; }
    public UUID getOrderId() { return orderId; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public Instant getUsedAt() { return usedAt; }
}
