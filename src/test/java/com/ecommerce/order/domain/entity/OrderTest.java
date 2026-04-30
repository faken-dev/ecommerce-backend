package com.ecommerce.order.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order Entity — Domain Tests")
class OrderTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("should create order with PENDING status and calculated total")
        void createsCorrectly() {
            var items = List.of(
                    com.ecommerce.order.domain.entity.OrderItem.create(
                            null, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "Product A", "SKU-A", null,
                            null, null, 2, new BigDecimal("50000")
                    ),
                    com.ecommerce.order.domain.entity.OrderItem.create(
                            null, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "Product B", "SKU-B", null,
                            null, null, 1, new BigDecimal("30000")
                    )
            );

            Order order = Order.createOrder(
                    Fixtures.USER_ID, Fixtures.SELLER_ID, Fixtures.ADDRESS_ID,
                    items,
                    new BigDecimal("130000"),  // subtotal
                    new BigDecimal("20000"),     // shippingFee
                    new BigDecimal("10000"),     // taxAmount
                    new BigDecimal("5000"),      // discountAmount
                    "VND",
                    "192.168.1.1", "Mozilla/5.0"
            );

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getPaymentStatus()).isEqualTo(com.ecommerce.order.domain.entity.PaymentStatus.PENDING);
            assertThat(order.getBuyerId()).isEqualTo(Fixtures.USER_ID);
            assertThat(order.getSellerId()).isEqualTo(Fixtures.SELLER_ID);
            assertThat(order.getItems()).hasSize(2);
            // total = 130000 + 20000 + 10000 - 5000 = 155000
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("155000"));
            assertThat(order.getCancelWindowSec()).isEqualTo(1800L);
        }

        @Test
        @DisplayName("should assign orderId to each item")
        void assignsOrderId() {
            var item = com.ecommerce.order.domain.entity.OrderItem.create(
                    null, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S", null, null, null, 1,
                    new BigDecimal("10000"));

            Order order = Order.createOrder(
                    Fixtures.USER_ID, Fixtures.SELLER_ID, Fixtures.ADDRESS_ID,
                    List.of(item),
                    new BigDecimal("10000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "VND", "ip", "ua"
            );

            assertThat(item.getOrderId()).isEqualTo(order.getId());
        }

        @Test
        @DisplayName("should add initial status history entry")
        void addsStatusHistory() {
            var item = com.ecommerce.order.domain.entity.OrderItem.create(
                    null, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S", null, null, null, 1,
                    new BigDecimal("10000"));

            Order order = Order.createOrder(
                    Fixtures.USER_ID, Fixtures.SELLER_ID, Fixtures.ADDRESS_ID,
                    List.of(item),
                    new BigDecimal("10000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "VND", "ip", "ua"
            );

            assertThat(order.getStatusHistory()).hasSize(1);
            assertThat(order.getStatusHistory().get(0).getToStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    // ── Status Transitions ───────────────────────────────────────────────────

    @Nested
    @DisplayName("transitionTo()")
    class TransitionTo {

        @Test
        @DisplayName("PENDING can transition to CONFIRMED")
        void pendingToConfirmed() {
            Order order = Fixtures.anOrder();
            order.transitionTo(OrderStatus.CONFIRMED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PENDING can transition to CANCELLED")
        void pendingToCancelled() {
            Order order = Fixtures.anOrder();
            order.transitionTo(OrderStatus.CANCELLED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw for invalid transition")
        void invalidTransition() {
            Order order = Fixtures.anOrder();

            assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPED))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
        }
    }

    // ── confirmPayment ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("confirmPayment()")
    class ConfirmPayment {

        @Test
        @DisplayName("should transition to CONFIRMED and set paymentStatus to PAID")
        void confirmsPayment() {
            Order order = Fixtures.anOrder();

            order.confirmPayment(java.util.UUID.randomUUID());

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getPaymentStatus()).isEqualTo(com.ecommerce.order.domain.entity.PaymentStatus.PAID);
        }

        @Test
        @DisplayName("should throw when not in PENDING status")
        void throwsFromNonPending() {
            Order order = Fixtures.anOrder();
            order.transitionTo(OrderStatus.CONFIRMED);

            assertThatThrownBy(() -> order.confirmPayment(java.util.UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
        }
    }

    // ── Cancellation ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("should transition to CANCELLED")
        void genericCancel() {
            Order order = Fixtures.anOrder();
            order.cancel(Fixtures.ADMIN_ID);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw ORDER_CANNOT_BE_CANCELLED for PROCESSING order")
        void cannotCancelTerminal() {
            // PENDING → CONFIRMED → PROCESSING (valid transitions)
            Order order = Fixtures.anOrder();
            order.transitionTo(OrderStatus.CONFIRMED);
            order.transitionTo(OrderStatus.PROCESSING);
            // PROCESSING cannot transition to CANCELLED
            assertThatThrownBy(() -> order.cancel(Fixtures.ADMIN_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
    }

    @Nested
    @DisplayName("cancelByBuyer()")
    class CancelByBuyer {

        @Test
        @DisplayName("should succeed when buyer matches and within cancel window")
        void buyerCancelsWithinWindow() {
            Order order = Fixtures.anOrder();

            order.cancelByBuyer(Fixtures.USER_ID);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw when buyerId does not match")
        void wrongBuyer() {
            Order order = Fixtures.anOrder();

            assertThatThrownBy(() -> order.cancelByBuyer(Fixtures.OTHER_USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isBuyerCancellable() / isSellerCancellable()")
    class Cancellability {

        @Test
        @DisplayName("new order is buyer-cancellable within 30 min window")
        void newOrderBuyerCancellable() {
            Order order = Fixtures.anOrder();
            assertThat(order.isBuyerCancellable()).isTrue();
        }

        @Test
        @DisplayName("PENDING and CONFIRMED orders are seller-cancellable")
        void sellerCancellable() {
            Order pending = Fixtures.anOrder();
            assertThat(pending.isSellerCancellable()).isTrue();

            pending.transitionTo(OrderStatus.CONFIRMED);
            assertThat(pending.isSellerCancellable()).isTrue();

            pending.transitionTo(OrderStatus.PROCESSING);
            assertThat(pending.isSellerCancellable()).isFalse();
        }
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("setBuyerNote()")
    class Notes {

        @Test
        @DisplayName("should set buyer note")
        void setsNote() {
            Order order = Fixtures.anOrder();
            order.setBuyerNote("Please deliver after 6pm");

            assertThat(order.getBuyerNote()).isEqualTo("Please deliver after 6pm");
        }
    }

    // ── Soft Delete ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("should set deletedAt")
        void setsDeletedAt() {
            Order order = Fixtures.anOrder();
            order.softDelete();
            assertThat(order.getDeletedAt()).isNotNull();
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toCreatedEvent contains order data")
        void createdEvent() {
            var item = com.ecommerce.order.domain.entity.OrderItem.create(
                    null, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S", null, null, null, 1,
                    new BigDecimal("10000"));
            Order order = Order.createOrder(
                    Fixtures.USER_ID, Fixtures.SELLER_ID, Fixtures.ADDRESS_ID,
                    List.of(item),
                    new BigDecimal("10000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "VND", "ip", "ua"
            );

            var event = order.toCreatedEvent();

            assertThat(event.orderId()).isEqualTo(order.getId());
            assertThat(event.buyerId()).isEqualTo(Fixtures.USER_ID);
            assertThat(event.totalAmount()).isNotNull();
        }

        @Test
        @DisplayName("toCancelledEvent contains cancel data")
        void cancelledEvent() {
            Order order = Fixtures.anOrder();
            order.cancel(Fixtures.ADMIN_ID);
            var event = order.toCancelledEvent(Fixtures.ADMIN_ID);
            assertThat(event.cancelledBy()).isEqualTo(Fixtures.ADMIN_ID);
        }
    }
}