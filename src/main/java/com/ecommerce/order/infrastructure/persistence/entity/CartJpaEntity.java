package com.ecommerce.order.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders_carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartJpaEntity extends AuditableJpaEntity {

    /**
     * Optimistic locking to prevent race-condition data loss when concurrent
     * {@link com.ecommerce.order.application.usecase.AddToCartUseCase} calls
     * read-modify-write the same cart. The second concurrent request will
     * receive an {@link jakarta.persistence.OptimisticLockException} and the
     * transaction will roll back — the user can retry.
     */
    @Version
    private Long version;

    @Column(name = "buyer_id", nullable = false)
    private java.util.UUID buyerId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItemJpaEntity> items = new ArrayList<>();

    /** Bidirectional helper — keeps {@code item.cart} in sync. */
    public void addItem(CartItemJpaEntity item) {
        this.items.add(item);
        item.setCart(this);
    }
}
