package com.ecommerce.shipping.application.dto;

import com.ecommerce.shipping.domain.ShipmentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ShipmentResponse {
    private UUID id;
    private UUID orderId;
    private String trackingNumber;
    private String carrierName;
    private ShipmentStatus status;
    private String recipientName;
    private String phone;
    private String fullAddress;
    private BigDecimal shippingFee;
    private OffsetDateTime estimatedDeliveryDate;
    private OffsetDateTime deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;
}
