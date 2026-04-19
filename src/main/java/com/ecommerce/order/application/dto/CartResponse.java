package com.ecommerce.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartResponse {
    private UUID id;
    private UUID buyerId;
    private int itemCount;
    private BigDecimal subtotal;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CartItemResponse> items;

    @Getter
    @Builder
    public static class CartItemResponse {
        private UUID id;
        private UUID productId;
        private UUID variantId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private Instant addedAt;
    }
}