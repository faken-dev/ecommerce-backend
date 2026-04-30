package com.ecommerce.voucher.application.dto;

import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result of validating (or applying) a voucher against a cart/order.
 * Aligned with frontend DiscountValidationResult.
 */
public record DiscountValidationResult(
        boolean valid,
        String code,
        BigDecimal discountAmount,
        String message,
        VoucherResponse voucher,  // added to match frontend
        
        // Internal metadata (optional but helpful)
        UUID voucherId,
        VoucherType type,
        VoucherScope scope,
        String description
) {
    /**
     * Creates a result where the voucher is NOT applicable.
     */
    public static DiscountValidationResult notApplicable(String message) {
        return new DiscountValidationResult(
                false, null, BigDecimal.ZERO, message, null,
                null, null, null, null);
    }

    /**
     * Creates a result where the voucher IS applicable with a discount.
     */
    public static DiscountValidationResult applicable(
            Voucher voucherEntity,
            BigDecimal discountAmount,
            String description) {
        
        return new DiscountValidationResult(
                true,
                voucherEntity.getCode(),
                discountAmount,
                null,
                VoucherResponse.from(voucherEntity),
                voucherEntity.getId(),
                voucherEntity.getType(),
                voucherEntity.getScope(),
                description);
    }
}
