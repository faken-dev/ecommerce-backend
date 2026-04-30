package com.ecommerce.shipping.application.usecase;

import com.ecommerce.shipping.application.dto.ShipmentResponse;
import com.ecommerce.shipping.domain.ShippingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListShipmentsUseCase {
    private final ShippingRepository shippingRepository;

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> execute(Pageable pageable) {
        return shippingRepository.findAll(pageable)
                .map(s -> ShipmentResponse.builder()
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
                        .build());
    }
}
