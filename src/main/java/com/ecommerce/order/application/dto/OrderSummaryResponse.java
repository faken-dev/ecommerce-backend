package com.ecommerce.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class OrderSummaryResponse {
    private UUID id;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentStatus;
    private int itemCount;
    private Instant createdAt;
    private Instant updatedAt;
}