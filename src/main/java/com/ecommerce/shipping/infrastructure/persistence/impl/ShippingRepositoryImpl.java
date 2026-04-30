package com.ecommerce.shipping.infrastructure.persistence.impl;

import com.ecommerce.shipping.domain.Shipment;
import com.ecommerce.shipping.domain.ShippingAddress;
import com.ecommerce.shipping.domain.ShippingRepository;
import com.ecommerce.shipping.infrastructure.persistence.entity.ShipmentEntity;
import com.ecommerce.shipping.infrastructure.persistence.entity.ShippingAddressEmbeddable;
import com.ecommerce.shipping.infrastructure.persistence.repository.JpaShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShippingRepositoryImpl implements ShippingRepository {
    private final JpaShipmentRepository jpaRepository;

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentEntity entity = toEntity(shipment);
        ShipmentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return jpaRepository.findByTrackingNumber(trackingNumber).map(this::toDomain);
    }

    @Override
    public List<Shipment> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<Shipment> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    private ShipmentEntity toEntity(Shipment d) {
        return ShipmentEntity.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .trackingNumber(d.getTrackingNumber())
                .carrierName(d.getCarrierName())
                .status(d.getStatus())
                .address(ShippingAddressEmbeddable.builder()
                        .recipientName(d.getAddress().getRecipientName())
                        .phone(d.getAddress().getPhone())
                        .street(d.getAddress().getStreet())
                        .district(d.getAddress().getDistrict())
                        .city(d.getAddress().getCity())
                        .province(d.getAddress().getProvince())
                        .country(d.getAddress().getCountry())
                        .postalCode(d.getAddress().getPostalCode())
                        .build())
                .weight(d.getWeight())
                .shippingFee(d.getShippingFee())
                .estimatedDeliveryDate(d.getEstimatedDeliveryDate())
                .deliveredAt(d.getDeliveredAt())
                .build();
    }

    private Shipment toDomain(ShipmentEntity e) {
        return Shipment.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .trackingNumber(e.getTrackingNumber())
                .carrierName(e.getCarrierName())
                .status(e.getStatus())
                .address(ShippingAddress.builder()
                        .recipientName(e.getAddress().getRecipientName())
                        .phone(e.getAddress().getPhone())
                        .street(e.getAddress().getStreet())
                        .district(e.getAddress().getDistrict())
                        .city(e.getAddress().getCity())
                        .province(e.getAddress().getProvince())
                        .country(e.getAddress().getCountry())
                        .postalCode(e.getAddress().getPostalCode())
                        .build())
                .weight(e.getWeight())
                .shippingFee(e.getShippingFee())
                .estimatedDeliveryDate(e.getEstimatedDeliveryDate())
                .deliveredAt(e.getDeliveredAt())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().toInstant() : null)
                .updatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toInstant() : null)
                .build();
    }
}
