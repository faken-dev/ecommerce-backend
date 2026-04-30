package com.ecommerce.voucher.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for applying (committing) a voucher to an order.
 */
public record ApplyVoucherRequest(
        @NotBlank(message = "Voucher code is required")
        String code,

        UUID orderId,

        BigDecimal subtotal,
        BigDecimal shippingFee,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {}
