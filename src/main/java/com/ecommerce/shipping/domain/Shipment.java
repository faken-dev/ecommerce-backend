package com.ecommerce.shipping.domain;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Shipment Aggregate Root.
 * Manages the shipping process for a specific order.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Shipment extends AuditableEntity {
    private UUID orderId;
    private String trackingNumber;
    private String carrierName; // e.g., GHTK, GHN, FedEx
    private ShipmentStatus status;
    private ShippingAddress address;
    private BigDecimal weight; // in kg
    private BigDecimal shippingFee;
    private OffsetDateTime estimatedDeliveryDate;
    private OffsetDateTime deliveredAt;

    /**
     * Logic for status transition. 
     */
    public void updateStatus(ShipmentStatus newStatus) {
        if (this.status == ShipmentStatus.DELIVERED || this.status == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of a finished shipment.");
        }
        
        // Basic validation for state machine
        if (newStatus == ShipmentStatus.DELIVERED) {
            this.deliveredAt = OffsetDateTime.now();
        }
        
        this.status = newStatus;
        this.touchUpdate();
    }

    public void assignTracking(String carrierName, String trackingNumber) {
        this.carrierName = carrierName;
        this.trackingNumber = trackingNumber;
        if (this.status == ShipmentStatus.PENDING) {
            this.status = ShipmentStatus.PROCESSING;
        }
        this.touchUpdate();
    }
}
