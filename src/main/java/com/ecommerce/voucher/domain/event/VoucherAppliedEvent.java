package com.ecommerce.voucher.domain.event;

import com.ecommerce.voucher.domain.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a voucher is successfully applied to an order.
 * Consumed by Order module to apply discount to the order total.
 */
public record VoucherAppliedEvent(
        UUID voucherId,
        String voucherCode,
        UUID orderId,
        UUID userId,
        VoucherType type,
        BigDecimal discountAmount,
        Instant occurredAt
) {}
