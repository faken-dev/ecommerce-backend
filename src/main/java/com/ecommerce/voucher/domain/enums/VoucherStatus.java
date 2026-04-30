package com.ecommerce.voucher.domain.enums;

/**
 * Defines the status of a voucher, indicating its current state and whether it can be used.
 * - ACTIVE: The voucher is active and can be used by customers.
 * - EXPIRED: The voucher has passed its expiration date and can no longer be used.
 * - DISABLED: The voucher has been manually disabled by an administrator and cannot be used.
 * - PENDING: The voucher is pending activation and cannot be used until it becomes active.
 * - DEPLETED: The voucher has reached its usage limit (e.g., max redeem count) and can no longer be used.
 */
public enum VoucherStatus {

    ACTIVE,
    EXPIRED,
    DISABLED,
    PENDING,
    DEPLETED;

    public boolean isUsable() {
        return this == ACTIVE;
    }

    public boolean isTerminal() {
        return this == EXPIRED || this == DISABLED || this == DEPLETED;
    }
}
