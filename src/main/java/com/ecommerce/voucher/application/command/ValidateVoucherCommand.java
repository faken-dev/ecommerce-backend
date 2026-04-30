package com.ecommerce.voucher.application.command;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ValidateVoucherCommand(
        String code,
        UUID userId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {}
