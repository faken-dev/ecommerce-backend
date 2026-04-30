package com.ecommerce.order.infrastructure.persistence.impl;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.OrderStatusHistoryJpaEntity;
import com.ecommerce.order.infrastructure.persistence.mapper.OrderDomainMapper;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ecommerce.order.infrastructure.persistence.repository.OrderStatusHistoryJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderStatusHistoryJpaRepository statusHistoryJpaRepository;
    private final OrderDomainMapper mapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toOrderJpa(order);
        entity.getItems().clear();
        for (var item : order.getItems()) {
            OrderItemJpaEntity itemEntity = mapper.toOrderItemJpa(item);
            entity.addItem(itemEntity);
        }
        if (entity.getId() != null) {
            Optional<OrderJpaEntity> existing = orderJpaRepository.findById(entity.getId());
            existing.ifPresent(e -> entity.setId(e.getId()));
        }
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        Order result = mapper.toOrder(saved);
        result.getItems().clear();
        for (OrderItemJpaEntity savedItem : saved.getItems()) {
            result.addItem(mapper.toOrderItem(savedItem));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .filter(e -> e.getDeletedAt() == null)
                .map(entity -> {
                    Order order = mapper.toOrder(entity);
                    order.getItems().clear();
                    entity.getItems().forEach(i -> order.addItem(mapper.toOrderItem(i)));
                    return order;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdWithItems(UUID orderId) {
        return orderJpaRepository.findByIdWithItems(orderId)
                .filter(e -> e.getDeletedAt() == null)
                .map(entity -> {
                    Order order = mapper.toOrder(entity);
                    order.getItems().clear();
                    entity.getItems().forEach(i -> order.addItem(mapper.toOrderItem(i)));
                    return order;
                });
    }

    @Override
    public void saveStatusHistory(UUID orderId, String fromStatus, String toStatus,
            UUID changedBy, String changedByRole, String reason, String metadata) {
        OrderStatusHistoryJpaEntity entity = OrderStatusHistoryJpaEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedByRole(changedByRole)
                .reason(reason)
                .metadata(metadata)
                .createdAt(Instant.now())
                .build();
        statusHistoryJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID orderId) {
        return orderJpaRepository.existsByIdAndDeletedAtIsNull(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndStatus(UUID orderId, OrderStatus status) {
        return orderJpaRepository.findById(orderId)
                .filter(e -> e.getDeletedAt() == null)
                .map(e -> e.getStatus().equals(status.name()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByBuyerId(UUID buyerId, Pageable pageable) {
        return orderJpaRepository.findByBuyerId(buyerId, pageable).map(mapper::toOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findBySellerId(UUID sellerId, Pageable pageable) {
        return orderJpaRepository.findBySellerId(sellerId, pageable).map(mapper::toOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByBuyerIdAndStatus(UUID buyerId, String status, Pageable pageable) {
        return orderJpaRepository.findByBuyerIdAndStatus(buyerId, status, pageable)
                .map(mapper::toOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable) {
        return orderJpaRepository.findBySellerIdAndStatus(sellerId, status, pageable)
                .map(mapper::toOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByStatus(String status, Pageable pageable) {
        return orderJpaRepository.findByStatusAndDeletedAtIsNull(status, pageable).map(mapper::toOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAllByDeletedAtIsNull(pageable).map(mapper::toOrder);
    }

    @Override
    public void deleteById(UUID orderId) {
        orderJpaRepository.findById(orderId).ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            orderJpaRepository.save(entity);
        });
    }

    @Override
    public long countByStatus(String status) {
        return orderJpaRepository.countByStatusAndDeletedAtIsNull(status);
    }

    @Override
    public long countPaidOrdersAfter(Instant after) {
        return orderJpaRepository.countPaidOrdersAfter(after);
    }

    @Override
    public BigDecimal sumTotalAmountAfter(Instant after) {
        return orderJpaRepository.sumTotalAmountAfter(after);
    }

    @Override
    public BigDecimal sumTotalAmountBetween(Instant start, Instant end) {
        return orderJpaRepository.sumTotalAmountBetween(start, end);
    }

    @Override
    public boolean hasPurchasedProduct(UUID buyerId, UUID productId) {
        return orderJpaRepository.hasPurchasedProduct(buyerId, productId);
    }

    @Override
    public long countBySeller(UUID sellerId) {
        return orderJpaRepository.countBySellerIdAndDeletedAtIsNull(sellerId);
    }

    @Override
    public long countBySellerAndStatus(UUID sellerId, OrderStatus status) {
        return orderJpaRepository.countBySellerIdAndStatusAndDeletedAtIsNull(sellerId, status.name());
    }

    @Override
    public long countBySellerAndCreatedAtAfter(UUID sellerId, Instant after) {
        return orderJpaRepository.countBySellerIdAndCreatedAtAfter(sellerId, after);
    }

    @Override
    public BigDecimal sumTotalAmountBySellerAfter(UUID sellerId, Instant after) {
        return orderJpaRepository.sumTotalAmountBySellerAfter(sellerId, after);
    }
}
