package com.ecommerce.wishlist.application.usecase;

import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.wishlist.domain.entity.WishlistItem;
import com.ecommerce.wishlist.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddToWishlistUseCase {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void execute(UUID userId, UUID productId) {
        // 1. Check if product exists
        if (!productRepository.findById(productId).isPresent()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }

        // 2. Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // Already exists, do nothing or throw exception
        }

        // 3. Save to wishlist
        WishlistItem item = WishlistItem.create(userId, productId);
        wishlistRepository.save(item);
    }
}
