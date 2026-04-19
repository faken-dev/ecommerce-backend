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
    private UUID sellerId;
    private String status;
    private UUID shippingAddressId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
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
    }
}