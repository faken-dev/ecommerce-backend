package com.ecommerce.voucher.application.dto;

import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for voucher data.
 * Aligned with frontend VoucherDTO.
 */
public record VoucherResponse(
        UUID id,
        String code,
        String name,
        String description,
        VoucherType type,
        VoucherScope scope,
        VoucherStatus status,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        int maxUsageTotal,
        int maxUsagePerUser,
        int currentUsageCount,
        int remainingUsage,      // added to match frontend
        Instant validFrom,
        Instant validTo,
        Set<UUID> applicableProductIds,
        Set<UUID> applicableCategoryIds,
        UUID sellerId,
        Instant createdAt,
        boolean requiresCollection
) {
    /**
     * Creates from domain entity.
     */
    public static VoucherResponse from(Voucher voucher) {
        int remaining = -1;
        if (voucher.getMaxUsageTotal() > 0) {
            remaining = Math.max(0, voucher.getMaxUsageTotal() - voucher.getCurrentUsageCount());
        }

        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getName(),
                voucher.getDescription(),
                voucher.getType(),
                voucher.getScope(),
                voucher.getStatus(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                voucher.getMinOrderAmount(),
                voucher.getMaxUsageTotal(),
                voucher.getMaxUsagePerUser(),
                voucher.getCurrentUsageCount(),
                remaining,
                voucher.getValidFrom(),
                voucher.getValidTo(),
                voucher.getApplicableProductIds(),
                voucher.getApplicableCategoryIds(),
                voucher.getSellerId(),
                voucher.getCreatedAt(),
                voucher.isRequiresCollection()
        );
    }
}
