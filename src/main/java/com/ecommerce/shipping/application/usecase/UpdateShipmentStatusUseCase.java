package com.ecommerce.shipping.application.usecase;

import com.ecommerce.shipping.application.dto.ShipmentResponse;
import com.ecommerce.shipping.domain.Shipment;
import com.ecommerce.shipping.domain.ShipmentStatus;
import com.ecommerce.shipping.domain.ShippingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateShipmentStatusUseCase {
    private final ShippingRepository shippingRepository;

    @Transactional
    public ShipmentResponse execute(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shippingRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

        shipment.updateStatus(newStatus);
        Shipment saved = shippingRepository.save(shipment);

        return mapToResponse(saved);
    }

    private ShipmentResponse mapToResponse(Shipment s) {
        return ShipmentResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .status(s.getStatus())
                .trackingNumber(s.getTrackingNumber())
                .carrierName(s.getCarrierName())
                .recipientName(s.getAddress().getRecipientName())
                .phone(s.getAddress().getPhone())
                .fullAddress(s.getAddress().getFormattedAddress())
                .shippingFee(s.getShippingFee())
                .estimatedDeliveryDate(s.getEstimatedDeliveryDate())
                .deliveredAt(s.getDeliveredAt())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
