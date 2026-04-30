package com.ecommerce.order.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderResponse {
    private UUID id;
    private UUID buyerId;
    private String buyerName;
    private UUID sellerId;
    private String sellerName;
    private String status;
    private UUID shippingAddressId;
    private String shippingAddress;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentMethodName;
    private String paymentStatus;
    private UUID paymentId;
    private String buyerNote;
    private String sellerNote;
    private String shippingCarrier;
    private String trackingNumber;
    private long cancelWindowSec;
    private boolean buyerCancellable;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
    private List<StatusHistoryResponse> statusHistory;

    @Getter
    @Setter
    @Builder
    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productSku;
        private String productImageUrl;
        private UUID variantId;
        private String variantTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal discountAmount;
        private int refundedQuantity;
        private BigDecimal refundedAmount;

        public UUID getId() { return id; }
        public UUID getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductSku() { return productSku; }
        public String getProductImageUrl() { return productImageUrl; }
        public UUID getVariantId() { return variantId; }
        public String getVariantTitle() { return variantTitle; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public int getRefundedQuantity() { return refundedQuantity; }
        public BigDecimal getRefundedAmount() { return refundedAmount; }
    }

    @Getter
    @Setter
    @Builder
    public static class StatusHistoryResponse {
        private UUID id;
        private String fromStatus;
        private String toStatus;
        private UUID changedBy;
        private String changedByRole;
        private String reason;
        private Instant createdAt;

        public UUID getId() { return id; }
        public String getFromStatus() { return fromStatus; }
        public String getToStatus() { return toStatus; }
        public UUID getChangedBy() { return changedBy; }
        public String getChangedByRole() { return changedByRole; }
        public String getReason() { return reason; }
        public Instant getCreatedAt() { return createdAt; }
    }

    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public UUID getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getStatus() { return status; }
    public UUID getShippingAddressId() { return shippingAddressId; }
    public String getShippingAddress() { return shippingAddress; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentMethodName() { return paymentMethodName; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getBuyerNote() { return buyerNote; }
    public String getSellerNote() { return sellerNote; }
    public String getShippingCarrier() { return shippingCarrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public long getCancelWindowSec() { return cancelWindowSec; }
    public boolean isBuyerCancellable() { return buyerCancellable; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getPaymentId() { return paymentId; }
    public List<OrderItemResponse> getItems() { return items; }
    public List<StatusHistoryResponse> getStatusHistory() { return statusHistory; }
}
