package com.ecommerce.voucher.domain.event;

import com.ecommerce.voucher.domain.enums.VoucherStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when voucher status changes (activated, disabled, expired).
 */
public record VoucherStatusChangedEvent(
        UUID voucherId,
        String voucherCode,
        VoucherStatus previousStatus,
        VoucherStatus newStatus,
        Instant occurredAt
) {}
