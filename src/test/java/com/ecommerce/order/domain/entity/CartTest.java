package com.ecommerce.order.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cart Entity — Domain Tests")
class CartTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createCart()")
    class CreateCart {

        @Test
        @DisplayName("should create empty cart for buyer")
        void createsEmpty() {
            Cart cart = Fixtures.aCart();

            assertThat(cart.getBuyerId()).isEqualTo(Fixtures.USER_ID);
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.getItemCount()).isZero();
            assertThat(cart.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("should add new item to cart")
        void addsNewItem() {
            Cart cart = Fixtures.aCart();

            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 3, new BigDecimal("50000"));

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItemCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should increase quantity when same product already in cart")
        void increasesQuantity() {
            Cart cart = Fixtures.aCart();
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 2, new BigDecimal("50000"));

            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 3, new BigDecimal("50000"));

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItemCount()).isEqualTo(5);
        }
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateItemQuantity()")
    class UpdateQuantity {

        @Test
        @DisplayName("should update quantity of existing item")
        void updatesQuantity() {
            Cart cart = Fixtures.aCart();
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 2, new BigDecimal("50000"));

            cart.updateItemQuantity(Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID, 10);

            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("should throw when item not found")
        void throwsWhenNotFound() {
            Cart cart = Fixtures.aCart();

            assertThatThrownBy(() -> cart.updateItemQuantity(
                    Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart")
        void removesItem() {
            Cart cart = Fixtures.aCart();
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 2, new BigDecimal("50000"));

            cart.removeItem(Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID);

            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should throw when item not found")
        void throwsWhenNotFound() {
            Cart cart = Fixtures.aCart();

            assertThatThrownBy(() -> cart.removeItem(Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── clearCart ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("should remove all items")
        void clearsAll() {
            Cart cart = Fixtures.aCart();
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Test Product", "image.jpg", Fixtures.VARIANT_ID, "Test Variant", 2, new BigDecimal("50000"));
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Another Product", "image2.jpg", null, null, 1, new BigDecimal("30000"));

            cart.clearCart();

            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.getItemCount()).isZero();
        }
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSubtotal()")
    class GetSubtotal {

        @Test
        @DisplayName("should calculate subtotal correctly")
        void calculatesSubtotal() {
            Cart cart = Fixtures.aCart();
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Product 1", "img1.jpg", Fixtures.VARIANT_ID, "Var 1", 2, new BigDecimal("50000")); // 100000
            cart.addItem(Fixtures.PRODUCT_ID, Fixtures.USER_ID, "Product 2", "img2.jpg", null, null, 3, new BigDecimal("20000"));               // 60000

            assertThat(cart.getSubtotal()).isEqualByComparingTo(new BigDecimal("160000"));
        }
    }
}