package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByIdWithItems(UUID orderId);

    void saveStatusHistory(UUID orderId, String fromStatus, String toStatus,
                           UUID changedBy, String changedByRole, String reason, String metadata);

    boolean existsById(UUID orderId);

    boolean existsByIdAndStatus(UUID orderId, OrderStatus status);

    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);

    Page<Order> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(UUID buyerId, String status, Pageable pageable);

    Page<Order> findBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);
}
