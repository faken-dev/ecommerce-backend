package com.ecommerce.order.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Cart extends AuditableEntity {

    private UUID buyerId;
    private List<CartItem> items;

    // Factory
    public static Cart createCart(UUID buyerId) {
        return Cart.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .buyerId(buyerId)
                .items(new ArrayList<>())
                .build();
    }

    // Mutation Operations

    public void addItem(UUID productId, UUID variantId, int quantity, BigDecimal unitPrice) {
        CartItem existing = findItem(productId, variantId);
        if (existing != null) {
            existing.quantity += quantity;
        } else {
            CartItem item = CartItem.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .productId(productId)
                    .variantId(variantId)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .addedAt(Instant.now())
                    .build();
            this.items.add(item);
        }
    }

    public void updateItemQuantity(UUID productId, UUID variantId, int newQty) {
        CartItem item = findItemOrThrow(productId, variantId);
        item.quantity = newQty;
    }

    public void removeItem(UUID productId, UUID variantId) {
        boolean removed = this.items.removeIf(
                item -> Objects.equals(item.productId, productId)
                        && Objects.equals(item.variantId, variantId));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Cart item not found for productId=" + productId + ", variantId=" + variantId);
        }
    }

    public void clearCart() {
        this.items.clear();
    }

    // Queries
    public int getItemCount() {
        return items.stream().mapToInt(item -> item.quantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helpers
    private CartItem findItem(UUID productId, UUID variantId) {
        return items.stream()
                .filter(item -> Objects.equals(item.productId, productId)
                        // If variantId is null, match only on productId.
                        // Otherwise match on both productId AND variantId.
                        && Objects.equals(item.variantId, variantId))
                .findFirst()
                .orElse(null);
    }

    private CartItem findItemOrThrow(UUID productId, UUID variantId) {
        CartItem item = findItem(productId, variantId);
        if (item == null) {
            throw new IllegalArgumentException(
                    "Cart item not found for productId=" + productId + ", variantId=" + variantId);
        }
        return item;
    }

    // Inner class — must be public static for OrderDomainMapper (different package)
    @Getter
    @Builder
    public static class CartItem {
        private UUID id;
        private UUID productId;
        private UUID variantId;
        private int quantity;
        private BigDecimal unitPrice;
        private Instant addedAt;
    }

    // Builder constructor
    @Builder
    public Cart(UUID id, UUID buyerId, List<CartItem> items,
                Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.buyerId = buyerId;
        this.items = items != null ? items : new ArrayList<>();
    }
}