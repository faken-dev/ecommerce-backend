package com.ecommerce.order.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItem Entity — Domain Tests")
class OrderItemTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create item with calculated totalPrice")
        void createsCorrectly() {
            OrderItem item = OrderItem.create(
                    Fixtures.ORDER_ID,
                    Fixtures.PRODUCT_ID,
                    Fixtures.CATEGORY_ID,
                    "iPhone 15 Pro",
                    "SKU-001",
                    "https://cdn.example.com/img.jpg",
                    Fixtures.VARIANT_ID,
                    "128GB Black",
                    3,
                    new BigDecimal("29990000")
            );

            assertThat(item.getOrderId()).isEqualTo(Fixtures.ORDER_ID);
            assertThat(item.getProductId()).isEqualTo(Fixtures.PRODUCT_ID);
            assertThat(item.getProductName()).isEqualTo("iPhone 15 Pro");
            assertThat(item.getQuantity()).isEqualTo(3);
            // totalPrice = 29990000 * 3
            assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("89970000"));
            assertThat(item.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.getRefundedQuantity()).isZero();
            assertThat(item.getRefundedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── applyDiscount ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("applyDiscount()")
    class ApplyDiscount {

        @Test
        @DisplayName("should reduce totalPrice by discount amount")
        void reducesTotalPrice() {
            OrderItem item = OrderItem.create(
                    Fixtures.ORDER_ID, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S",
                    null, null, null, 2, new BigDecimal("50000"));

            item.applyDiscount(new BigDecimal("5000"));

            // 50000 * 2 - 5000 = 95000
            assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("95000"));
            assertThat(item.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        }
    }

    // ── recordRefund ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("recordRefund()")
    class RecordRefund {

        @Test
        @DisplayName("should accumulate refunded quantity and amount")
        void accumulatesRefunds() {
            OrderItem item = OrderItem.create(
                    Fixtures.ORDER_ID, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S",
                    null, null, null, 5, new BigDecimal("10000"));

            item.recordRefund(2, new BigDecimal("20000"));

            assertThat(item.getRefundedQuantity()).isEqualTo(2);
            assertThat(item.getRefundedAmount()).isEqualByComparingTo(new BigDecimal("20000"));

            item.recordRefund(1, new BigDecimal("10000"));

            assertThat(item.getRefundedQuantity()).isEqualTo(3);
            assertThat(item.getRefundedAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        }
    }

    // ── isFullyRefunded ────────────────────────────────────────────────────

    @Nested
    @DisplayName("isFullyRefunded()")
    class IsFullyRefunded {

        @Test
        @DisplayName("returns true when refundedQuantity >= quantity")
        void fullyRefunded() {
            OrderItem item = OrderItem.create(
                    Fixtures.ORDER_ID, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S",
                    null, null, null, 5, new BigDecimal("10000"));
            item.recordRefund(5, new BigDecimal("50000"));

            assertThat(item.isFullyRefunded()).isTrue();
        }

        @Test
        @DisplayName("returns false when refundedQuantity < quantity")
        void partiallyRefunded() {
            OrderItem item = OrderItem.create(
                    Fixtures.ORDER_ID, Fixtures.PRODUCT_ID, Fixtures.CATEGORY_ID, "P", "S",
                    null, null, null, 5, new BigDecimal("10000"));
            item.recordRefund(2, new BigDecimal("20000"));

            assertThat(item.isFullyRefunded()).isFalse();
        }
    }
}