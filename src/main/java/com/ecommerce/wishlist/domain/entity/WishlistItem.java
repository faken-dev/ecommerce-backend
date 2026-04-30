package com.ecommerce.wishlist.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WishlistItem extends AuditableEntity {
    private UUID userId;
    private UUID productId;
    private Instant addedAt;

    public static WishlistItem create(UUID userId, UUID productId) {
        return WishlistItem.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .productId(productId)
                .addedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
