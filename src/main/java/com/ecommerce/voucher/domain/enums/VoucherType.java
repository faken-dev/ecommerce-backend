package com.ecommerce.voucher.domain.enums;

/**
 * Defines the type of a voucher, indicating how the discount is calculated.
 * - PERCENTAGE: A percentage discount on the total order value.
 * - FIXED_AMOUNT: A fixed amount discount on the total order value.
 * - FREE_SHIPPING: Waives the shipping fee for the order.
 * - BUY_X_GET_Y: A "Buy X Get Y" type of discount, where
 */
public enum VoucherType {

    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_SHIPPING,
    BUY_X_GET_Y
}
