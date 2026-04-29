package com.ecommerce.order.domain.entity;

import com.ecommerce.order.domain.event.OrderConfirmedEvent;
import com.ecommerce.order.domain.event.OrderCancelledEvent;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderStatusChangedEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Collections;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Order extends AuditableEntity {

    private UUID buyerId;
    private UUID sellerId;
    private OrderStatus status;
    private UUID shippingAddressId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String buyerNote;
    private String sellerNote;
    private String shippingCarrier;
    private String trackingNumber;
    private long cancelWindowSec;
    private String ipAddress;
    private String userAgent;
    private String appliedVoucherCode;
    private Instant deletedAt;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    @Builder.Default
    private List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    public static Order createOrder(UUID buyerId, UUID sellerId, UUID shippingAddressId,
            List<OrderItem> items, BigDecimal subtotal, BigDecimal shippingFee,
            BigDecimal taxAmount, BigDecimal discountAmount, String currency,
            String ipAddress, String userAgent) {

        UUID orderId = UuidCreator.getTimeOrderedEpoch();
        BigDecimal total = subtotal
                .add(shippingFee)
                .add(taxAmount)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

        items.forEach(item -> item.setOrderId(orderId));

        Order order = Order.builder()
                .id(orderId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .status(OrderStatus.PENDING)
                .shippingAddressId(shippingAddressId)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .totalAmount(total)
                .currency(currency)
                .paymentStatus(PaymentStatus.PENDING)
                .cancelWindowSec(1800L)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        items.forEach(order::addItem);
        order.addStatusHistoryEntry(null, OrderStatus.PENDING, orderId, "SYSTEM", "Order created", null);

        return order;
    }

    public void transitionTo(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "Cannot transition order from %s to %s".formatted(status, newStatus));
        }
        OrderStatus previous = this.status;
        this.status = newStatus;
        addStatusHistoryEntry(previous, newStatus, null, null, null, null);
    }

    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "Cannot confirm payment Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â order status is %s, expected PENDING".formatted(this.status));
        }
        this.status = OrderStatus.CONFIRMED;
        this.paymentStatus = PaymentStatus.PAID;
        addStatusHistoryEntry(OrderStatus.PENDING, OrderStatus.CONFIRMED, null, null, "Payment confirmed", null);
    }

    public void confirmPayment(UUID paymentId) {
        confirmPayment();
    }

    public void cancel(UUID cancelledBy) {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Order in status %s cannot be cancelled".formatted(status));
        }
        OrderStatus previous = this.status;
        this.status = OrderStatus.CANCELLED;
        addStatusHistoryEntry(previous, OrderStatus.CANCELLED, cancelledBy, null, "Cancelled", null);
    }

    public void cancelByBuyer(UUID buyerId) {
        if (!this.buyerId.equals(buyerId)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Only the order buyer can cancel");
        }
        if (!isBuyerCancellable()) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Order is not cancellable by buyer");
        }
        cancel(buyerId);
    }

    public boolean isBuyerCancellable() {
        return !status.isTerminal() && getElapsedSeconds() < cancelWindowSec;
    }

    public boolean isSellerCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private long getElapsedSeconds() {
        if (getCreatedAt() == null) return 0;
        return Duration.between(getCreatedAt(), Instant.now()).getSeconds();
    }

    public void addStatusHistoryEntry(OrderStatus fromStatus, OrderStatus toStatus,
            UUID changedBy, String changedByRole, String reason, String metadata) {
        StatusHistoryEntry entry = StatusHistoryEntry.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedByRole(changedByRole)
                .reason(reason)
                .metadata(metadata)
                .createdAt(Instant.now())
                .build();
        this.statusHistory.add(entry);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistoryEntry {
        private UUID id;
        private OrderStatus fromStatus;
        private OrderStatus toStatus;
        private UUID changedBy;
        private String changedByRole;
        private String reason;
        private String metadata;
        private Instant createdAt;
    }

    public void addItem(OrderItem item) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
    }

    public BigDecimal getOrderTotal() {
        return totalAmount;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public OrderCreatedEvent toCreatedEvent() {
        // Map OrderItems to OrderCreatedEvent.OrderItemData records
        List<OrderCreatedEvent.OrderItemData> itemData = this.items.stream()
                .map(item -> new OrderCreatedEvent.OrderItemData(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        // Collect distinct category IDs from items if available; for now, empty set as placeholder
        Set<UUID> categoryIds = Collections.emptySet();

        return new OrderCreatedEvent(
                getId(),
                buyerId,
                sellerId,
                totalAmount,
                discountAmount,
                appliedVoucherCode,
                itemData,
                categoryIds,
                Instant.now()
        );
    }

    public OrderCancelledEvent toCancelledEvent(UUID cancelledBy) {
        List<OrderCancelledEvent.OrderItemData> itemData = this.items.stream()
                .map(item -> new OrderCancelledEvent.OrderItemData(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity()
                ))
                .toList();
        return new OrderCancelledEvent(getId(), buyerId, cancelledBy, itemData, Instant.now());
    }

    public OrderConfirmedEvent toConfirmedEvent() {
        List<OrderConfirmedEvent.OrderItemData> itemData = this.items.stream()
                .map(item -> new OrderConfirmedEvent.OrderItemData(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity()
                ))
                .toList();
        return new OrderConfirmedEvent(
                getId(), buyerId, itemData, Instant.now());
    }

    public OrderStatusChangedEvent toStatusChangedEvent(OrderStatus from, OrderStatus to) {
        return new OrderStatusChangedEvent(getId(), from, to, Instant.now());
    }
}
