package com.ecommerce.voucher.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for validating (previewing) a voucher.
 */
public record ValidateVoucherRequest(
        @NotBlank(message = "Voucher code is required")
        String code,

        BigDecimal subtotal,
        BigDecimal shippingFee,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {}
