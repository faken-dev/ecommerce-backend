package com.ecommerce.voucher.domain.event;

import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new voucher is created.
 */
public record VoucherCreatedEvent(
        UUID voucherId,
        String code,
        String name,
        VoucherType type,
        VoucherScope scope,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        int maxUsageTotal,
        int maxUsagePerUser,
        Instant validFrom,
        Instant validTo,
        UUID sellerId,
        Instant occurredAt
) {}
