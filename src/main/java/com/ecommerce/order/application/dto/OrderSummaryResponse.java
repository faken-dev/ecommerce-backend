package com.ecommerce.order.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderSummaryResponse {
    private UUID id;
    private UUID buyerId;
    private String buyerName;
    private UUID sellerId;
    private String sellerName;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentStatus;
    private int itemCount;
    private UUID paymentId;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public UUID getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentStatus() { return paymentStatus; }
    public int getItemCount() { return itemCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getPaymentId() { return paymentId; }
}
