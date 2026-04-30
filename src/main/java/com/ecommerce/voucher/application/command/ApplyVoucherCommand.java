package com.ecommerce.voucher.application.command;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ApplyVoucherCommand(
        String code,
        UUID userId,
        UUID orderId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {}
