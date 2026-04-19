package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OrderStatusHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryJpaEntity, UUID> {

    List<OrderStatusHistoryJpaEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
