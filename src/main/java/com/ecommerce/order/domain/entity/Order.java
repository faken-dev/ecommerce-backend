package com.ecommerce.order.domain.entity;

import com.ecommerce.order.domain.event.OrderCancelledEvent;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderStatusChangedEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
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
    private Instant deletedAt;

    private final List<OrderItem> items = new ArrayList<>();
    private final List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static Order createOrder(UUID buyerId, UUID sellerId, UUID shippingAddressId,
            List<OrderItem> items, BigDecimal subtotal, BigDecimal shippingFee,
            BigDecimal taxAmount, BigDecimal discountAmount, String currency,
            String ipAddress, String userAgent) {

        UUID orderId = UuidCreator.getTimeOrderedEpoch();
        BigDecimal total = subtotal
                .add(shippingFee)
                .add(taxAmount)
                .subtract(discountAmount);

        // Wire orderId into each item
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
                .paymentMethod(null)
                .paymentStatus(PaymentStatus.PENDING)
                .buyerNote(null)
                .sellerNote(null)
                .shippingCarrier(null)
                .trackingNumber(null)
                .cancelWindowSec(1800L)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deletedAt(null)
                .build();
        // Items are added after construction since Order has no items field in builder
        items.forEach(order::addItem);

        order.addStatusHistoryEntry(null, OrderStatus.PENDING, orderId, "SYSTEM", "Order created", null);

        return order;
    }

    // ─── Status Transitions ───────────────────────────────────────────────────

    /**
     * Validates the state machine and transitions to the target status.
     *
     * @throws BusinessException with {@link ErrorCode#ORDER_INVALID_STATUS_TRANSITION}
     *                           when the transition is not allowed.
     */
    public void transitionTo(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "Cannot transition order from %s to %s".formatted(status, newStatus));
        }
        OrderStatus previous = this.status;
        this.status = newStatus;
        addStatusHistoryEntry(previous, newStatus, null, null, null, null);
    }

    /**
     * Confirms payment: PENDING → CONFIRMED and sets paymentStatus to PAID.
     *
     * @throws BusinessException with {@link ErrorCode#ORDER_INVALID_STATUS_TRANSITION}
     *                           when current status is not PENDING.
     */
    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "Cannot confirm payment — order status is %s, expected PENDING".formatted(this.status));
        }
        this.status = OrderStatus.CONFIRMED;
        this.paymentStatus = PaymentStatus.PAID;
        addStatusHistoryEntry(OrderStatus.PENDING, OrderStatus.CONFIRMED, null, null, "Payment confirmed", null);
    }

    // ─── Cancellation ─────────────────────────────────────────────────────────

    /**
     * Generic cancel guarded by the status state machine.
     *
     * @param cancelledBy UUID of the actor who cancelled
     * @throws BusinessException with {@link ErrorCode#ORDER_CANNOT_BE_CANCELLED}
     *                           when the transition is not allowed.
     */
    public void cancel(UUID cancelledBy) {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Order in status %s cannot be cancelled".formatted(status));
        }
        OrderStatus previous = this.status;
        this.status = OrderStatus.CANCELLED;
        addStatusHistoryEntry(previous, OrderStatus.CANCELLED, cancelledBy, null, "Cancelled", null);
    }

    /**
     * Buyer-initiated cancel — additionally validates the cancel window.
     *
     * @param buyerId the buyer's UUID (must match the order's buyerId)
     * @throws BusinessException with {@link ErrorCode#ORDER_CANNOT_BE_CANCELLED}
     *                           when outside the cancel window or status disallows it.
     */
    public void cancelByBuyer(UUID buyerId) {
        if (!this.buyerId.equals(buyerId)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Only the order buyer can cancel");
        }
        if (!isBuyerCancellable()) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Order is not cancellable by buyer (status=%s, elapsedSecs=%d, windowSecs=%d)"
                            .formatted(status, getElapsedSeconds(), cancelWindowSec));
        }
        cancel(buyerId);
    }

    /**
     * @return true if the buyer can still cancel within the configured cancel window
     *         and the order is not in a terminal status.
     */
    public boolean isBuyerCancellable() {
        return !status.isTerminal() && getElapsedSeconds() < cancelWindowSec;
    }

    /**
     * @return true if the seller can cancel (status PENDING or CONFIRMED).
     */
    public boolean isSellerCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private long getElapsedSeconds() {
        return Duration.between(getCreatedAt(), Instant.now()).getSeconds();
    }

    // ─── Status History ────────────────────────────────────────────────────────

    public void addStatusHistoryEntry(OrderStatus fromStatus, OrderStatus toStatus,
            UUID changedBy, String changedByRole, String reason, String metadata) {
        StatusHistoryEntry entry = new StatusHistoryEntry(
                UuidCreator.getTimeOrderedEpoch(),
                fromStatus,
                toStatus,
                changedBy,
                changedByRole,
                reason,
                metadata,
                Instant.now()
        );
        this.statusHistory.add(entry);
    }

    public record StatusHistoryEntry(
            UUID id,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            UUID changedBy,
            String changedByRole,
            String reason,
            String metadata,
            Instant createdAt
    ) {}

    // ─── Items ─────────────────────────────────────────────────────────────────

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public BigDecimal getOrderTotal() {
        return totalAmount;
    }

    // ─── Soft Delete ──────────────────────────────────────────────────────────

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    // ─── Notes ────────────────────────────────────────────────────────────────

    /**
     * Sets the buyer note after order creation.
     * Use this instead of rebuilding the Order via Builder,
     * which would lose items and status history.
     */
    public void setBuyerNote(String note) {
        this.buyerNote = note;
    }

    // ─── Domain Event Factories ────────────────────────────────────────────────
    // Convention: every mutating method that has external side-effects (events, DB)
    // should return a factory method that the application layer calls EventPublisher.publish() on.
    // Entity NEVER directly publishes events — that is the application layer's responsibility.

    /** Creates the {@link OrderCreatedEvent} after this order has been saved. */
    public OrderCreatedEvent toCreatedEvent() {
        return new OrderCreatedEvent(
                getId(),
                buyerId,
                sellerId,
                totalAmount,
                Instant.now());
    }

    /** Creates the {@link OrderCancelledEvent} when this order is cancelled. */
    public OrderCancelledEvent toCancelledEvent(UUID cancelledBy) {
        return new OrderCancelledEvent(getId(), buyerId, cancelledBy, Instant.now());
    }

    /** Creates the {@link OrderStatusChangedEvent} after a status transition completes. */
    public OrderStatusChangedEvent toStatusChangedEvent(OrderStatus from, OrderStatus to) {
        return new OrderStatusChangedEvent(getId(), from, to, Instant.now());
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    @Builder
    public Order(UUID id, UUID buyerId, UUID sellerId, OrderStatus status,
            UUID shippingAddressId, BigDecimal subtotal, BigDecimal shippingFee,
            BigDecimal taxAmount, BigDecimal discountAmount, BigDecimal totalAmount,
            String currency, String paymentMethod, PaymentStatus paymentStatus,
            String buyerNote, String sellerNote, String shippingCarrier,
            String trackingNumber, long cancelWindowSec, String ipAddress,
            String userAgent, Instant deletedAt,
            Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.status = status;
        this.shippingAddressId = shippingAddressId;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.buyerNote = buyerNote;
        this.sellerNote = sellerNote;
        this.shippingCarrier = shippingCarrier;
        this.trackingNumber = trackingNumber;
        this.cancelWindowSec = cancelWindowSec;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deletedAt = deletedAt;
    }
}