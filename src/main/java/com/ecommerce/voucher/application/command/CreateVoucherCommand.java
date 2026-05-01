package com.ecommerce.voucher.application.command;

import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateVoucherCommand(
        String code,
        String name,
        String description,
        VoucherType type,
        VoucherScope scope,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        int maxUsageTotal,
        int maxUsagePerUser,
        Instant validFrom,
        Instant validTo,
        Set<UUID> applicableProductIds,
        Set<UUID> applicableCategoryIds,
        UUID sellerId,
        Boolean requiresCollection
) {}
