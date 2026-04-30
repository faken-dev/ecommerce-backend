package com.ecommerce.order.application.port;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Port for querying voucher information from the Voucher module.
 */
public interface VoucherQueryPort {

    /**
     * Calculates the discount for a given voucher code and order details.
     */
    DiscountInfo calculateDiscount(String code, UUID userId, BigDecimal subtotal, BigDecimal shippingFee, Set<UUID> productIds, Set<UUID> categoryIds);

    record DiscountInfo(
            boolean applicable,
            BigDecimal discountAmount,
            String reason
    ) {}
}
