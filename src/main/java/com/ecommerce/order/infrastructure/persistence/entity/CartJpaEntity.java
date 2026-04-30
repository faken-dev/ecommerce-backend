package com.ecommerce.order.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders_carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartJpaEntity extends AuditableJpaEntity {

    @Version
    private Long version;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItemJpaEntity> items = new ArrayList<>();

    public void addItem(CartItemJpaEntity item) {
        this.items.add(item);
        item.setCart(this);
    }
}
