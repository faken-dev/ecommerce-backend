package com.ecommerce.voucher.domain.enums;

/**
 * Defines the scope of a voucher, indicating where and how it can be applied.
 * - GLOBAL: Can be applied to any order.
 * - SPECIFIC_PRODUCTS: Can only be applied to specific products.
 * - SPECIFIC_CATEGORIES: Can only be applied to specific product categories.
 * - NEW_USERS_ONLY: Can only be used by new users.
 * - EXISTING_USERS_ONLY: Can only be used by existing users.
 */
public enum VoucherScope {
    GLOBAL,
    SPECIFIC_PRODUCTS,
    SPECIFIC_CATEGORIES,
    NEW_USERS_ONLY,
    EXISTING_USERS_ONLY
}
