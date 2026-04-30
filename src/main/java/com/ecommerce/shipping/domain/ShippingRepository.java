package com.ecommerce.shipping.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ShippingRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID id);
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findAll();
    Page<Shipment> findAll(Pageable pageable);
}
