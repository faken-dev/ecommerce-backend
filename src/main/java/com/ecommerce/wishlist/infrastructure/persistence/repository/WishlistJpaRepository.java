package com.ecommerce.wishlist.infrastructure.persistence.repository;

import com.ecommerce.wishlist.infrastructure.persistence.entity.WishlistItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistJpaRepository extends JpaRepository<WishlistItemJpaEntity, UUID> {
    Optional<WishlistItemJpaEntity> findByUserIdAndProductId(UUID userId, UUID productId);
    List<WishlistItemJpaEntity> findByUserId(UUID userId);
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}
