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
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CartItemResponse> items;

    @Getter
    @Builder
    public static class CartItemResponse {
        private UUID id;
        private UUID productId;
        private UUID sellerId;
        private String productName;
        private String productImageUrl;
        private UUID variantId;
        private String variantTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private Instant addedAt;

        public UUID getId() { return id; }
        public UUID getProductId() { return productId; }
        public UUID getSellerId() { return sellerId; }
        public String getProductName() { return productName; }
        public String getProductImageUrl() { return productImageUrl; }
        public UUID getVariantId() { return variantId; }
        public String getVariantTitle() { return variantTitle; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public Instant getAddedAt() { return addedAt; }
    }

    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public int getItemCount() { return itemCount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<CartItemResponse> getItems() { return items; }
}
