package com.ecommerce.wishlist.presentation.controller;

import com.ecommerce.shared.infrastructure.security.SecurityUtils;
import com.ecommerce.wishlist.application.dto.WishlistResponse;
import com.ecommerce.wishlist.application.usecase.AddToWishlistUseCase;
import com.ecommerce.wishlist.application.usecase.GetWishlistUseCase;
import com.ecommerce.wishlist.application.usecase.RemoveFromWishlistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final AddToWishlistUseCase addToWishlistUseCase;
    private final RemoveFromWishlistUseCase removeFromWishlistUseCase;
    private final GetWishlistUseCase getWishlistUseCase;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable UUID productId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        addToWishlistUseCase.execute(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable UUID productId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        removeFromWishlistUseCase.execute(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(getWishlistUseCase.execute(userId));
    }
}
