package com.ecommerce.shipping.infrastructure.persistence.repository;

import com.ecommerce.shipping.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaShipmentRepository extends JpaRepository<ShipmentEntity, UUID> {
    Optional<ShipmentEntity> findByOrderId(UUID orderId);
    Optional<ShipmentEntity> findByTrackingNumber(String trackingNumber);
}
