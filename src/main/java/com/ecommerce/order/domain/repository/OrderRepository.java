package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

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

    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    void deleteById(UUID orderId);

    // Analytics Port Methods
    long countByStatus(String status);
    long countPaidOrdersAfter(Instant after);
    BigDecimal sumTotalAmountAfter(Instant after);
    BigDecimal sumTotalAmountBetween(Instant start, Instant end);
    boolean hasPurchasedProduct(UUID buyerId, UUID productId);

    // Seller Dashboard Port Methods
    long countBySeller(UUID sellerId);
    long countBySellerAndStatus(UUID sellerId, OrderStatus status);
    long countBySellerAndCreatedAtAfter(UUID sellerId, Instant after);
    BigDecimal sumTotalAmountBySellerAfter(UUID sellerId, Instant after);
}
