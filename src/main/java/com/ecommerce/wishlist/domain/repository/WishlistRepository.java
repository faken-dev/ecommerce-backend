package com.ecommerce.wishlist.domain.repository;

import com.ecommerce.wishlist.domain.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository {
    WishlistItem save(WishlistItem item);
    void delete(UUID userId, UUID productId);
    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId);
    List<WishlistItem> findByUserId(UUID userId);
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
}
