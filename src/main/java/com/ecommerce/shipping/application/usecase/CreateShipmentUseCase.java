package com.ecommerce.shipping.application.usecase;

import com.ecommerce.shipping.application.dto.CreateShipmentCommand;
import com.ecommerce.shipping.application.dto.ShipmentResponse;
import com.ecommerce.shipping.domain.CarrierProvider;
import com.ecommerce.shipping.domain.CarrierService;
import com.ecommerce.shipping.domain.Shipment;
import com.ecommerce.shipping.domain.ShipmentStatus;
import com.ecommerce.shipping.domain.ShippingAddress;
import com.ecommerce.shipping.domain.ShippingRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CreateShipmentUseCase {
    private final ShippingRepository shippingRepository;
    private final CarrierProvider carrierProvider;

    @Transactional
    public ShipmentResponse execute(CreateShipmentCommand request, String carrierName) {
        shippingRepository.findByOrderId(request.orderId()).ifPresent(s -> {
            throw new BusinessException(ErrorCode.SHIPMENT_ALREADY_EXISTS, 
                "Shipment already exists for order: " + request.orderId());
        });

        ShippingAddress address = ShippingAddress.builder()
                .recipientName(request.recipientName())
                .phone(request.phone())
                .street(request.street())
                .district(request.district())
                .city(request.city())
                .province(request.province())
                .country(request.country())
                .build();

        Shipment shipment = Shipment.builder()
                .orderId(request.orderId())
                .status(ShipmentStatus.PENDING)
                .address(address)
                .weight(request.weight())
                .shippingFee(request.shippingFee())
                .build();

        if (carrierName != null) {
            CarrierService carrier = carrierProvider.getCarrier(carrierName);
            // WARNING: External API call inside @Transactional. 
            // In a real production system, this should be handled asynchronously or via Outbox.
            String trackingNumber = carrier.createOrder(shipment);
            shipment.assignTracking(carrier.getCarrierName(), trackingNumber);
        }

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
