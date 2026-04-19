package com.ecommerce.order.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID sellerId,
        UUID shippingAddressId,
        List<OrderItemCommand> items,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        String currency,
        String buyerNote,
        String voucherCode
) {
    public record OrderItemCommand(
            UUID productId,
            UUID variantId,
            int quantity,
            BigDecimal unitPrice,
            String productName,
            String productSku,
            String productImageUrl,
            String variantTitle
    ) {}
}
