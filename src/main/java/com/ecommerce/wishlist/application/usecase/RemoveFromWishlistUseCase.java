package com.ecommerce.wishlist.application.usecase;

import com.ecommerce.wishlist.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveFromWishlistUseCase {

    private final WishlistRepository wishlistRepository;

    @Transactional
    public void execute(UUID userId, UUID productId) {
        wishlistRepository.delete(userId, productId);
    }
}
