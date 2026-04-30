package com.ecommerce.shipping.infrastructure.persistence.entity;

import com.ecommerce.shipping.domain.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipping_shipments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    private String trackingNumber;
    private String carrierName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Embedded
    private ShippingAddressEmbeddable address;

    @Column(precision = 38, scale = 2)
    private BigDecimal weight;

    @Column(precision = 38, scale = 2)
    private BigDecimal shippingFee;

    private OffsetDateTime estimatedDeliveryDate;
    private OffsetDateTime deliveredAt;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

}
