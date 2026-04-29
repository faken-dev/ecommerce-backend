package com.ecommerce.order.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Cart extends AuditableEntity {

    private UUID buyerId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public static Cart createCart(UUID buyerId) {
        return Cart.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .buyerId(buyerId)
                .items(new ArrayList<>())
                .build();
    }

    public void addItem(UUID productId, UUID sellerId, String productName, String productImageUrl,
                        UUID variantId, String variantTitle, int quantity, BigDecimal unitPrice) {
        CartItem existing = findItem(productId, variantId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CartItem item = CartItem.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .productId(productId)
                    .sellerId(sellerId)
                    .productName(productName)
                    .productImageUrl(productImageUrl)
                    .variantId(variantId)
                    .variantTitle(variantTitle)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .addedAt(Instant.now())
                    .build();
            this.items.add(item);
        }
    }

    public void updateItemQuantity(UUID productId, UUID variantId, int newQty) {
        CartItem item = findItemOrThrow(productId, variantId);
        item.setQuantity(newQty);
    }

    public void removeItem(UUID productId, UUID variantId) {
        boolean removed = this.items.removeIf(
                item -> Objects.equals(item.getProductId(), productId)
                        && Objects.equals(item.getVariantId(), variantId));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Cart item not found for productId=" + productId + ", variantId=" + variantId);
        }
    }

    public void clearCart() {
        this.items.clear();
    }

    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItem findItem(UUID productId, UUID variantId) {
        return items.stream()
                .filter(item -> Objects.equals(item.getProductId(), productId)
                        && Objects.equals(item.getVariantId(), variantId))
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItem {
        private UUID id;
        private UUID productId;
        private UUID sellerId;
        private String productName;
        private String productImageUrl;
        private UUID variantId;
        private String variantTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private Instant addedAt;
    }
}
