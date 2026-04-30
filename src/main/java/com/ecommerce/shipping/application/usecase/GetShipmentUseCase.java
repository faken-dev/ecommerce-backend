package com.ecommerce.shipping.application.usecase;

import com.ecommerce.shipping.application.dto.ShipmentResponse;
import com.ecommerce.shipping.domain.Shipment;
import com.ecommerce.shipping.domain.ShippingRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetShipmentUseCase {
    private final ShippingRepository shippingRepository;

    @Transactional(readOnly = true)
    public ShipmentResponse execute(UUID shipmentId) {
        Shipment shipment = shippingRepository.findById(shipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPMENT_NOT_FOUND, "Shipment not found: " + shipmentId));
        return mapToResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findByOrderId(UUID orderId) {
        return shippingRepository.findByOrderId(orderId)
                .map(this::mapToResponse)
                .orElse(null);
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
