package com.ecommerce.voucher.presentation.dto.request;

import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a new voucher via REST API.
 */
public record CreateVoucherRequest(

        @NotBlank(message = "Voucher code is required")
        @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
        String code,

        @NotBlank(message = "Voucher name is required")
        @Size(max = 200)
        String name,

        String description,

        @NotNull(message = "Voucher type is required")
        VoucherType type,

        @NotNull(message = "Voucher scope is required")
        VoucherScope scope,

        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be positive")
        BigDecimal discountValue,

        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,

        /** -1 = unlimited. 0 = disabled. Default = -1. */
        Integer maxUsageTotal,

        /** -1 = unlimited. Default = 1. */
        Integer maxUsagePerUser,

        Instant validFrom,
        Instant validTo,

        Set<UUID> applicableProductIds,
        Set<UUID> applicableCategoryIds,
        Boolean requiresCollection
) {}
