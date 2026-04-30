package com.ecommerce.wishlist.application.usecase;

import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.wishlist.application.dto.WishlistResponse;
import com.ecommerce.wishlist.domain.entity.WishlistItem;
import com.ecommerce.wishlist.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWishlistUseCase {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<WishlistResponse> execute(UUID userId) {
        List<WishlistItem> items = wishlistRepository.findByUserId(userId);
        if (items.isEmpty()) return List.of();

        List<UUID> productIds = items.stream()
                .map(WishlistItem::getProductId)
                .collect(Collectors.toList());

        // Batch fetch products for efficiency
        Map<UUID, Product> productMap = productRepository.findAllByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return items.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    if (product == null) return null;

                    return WishlistResponse.builder()
                            .productId(product.getId())
                            .name(product.getName())
                            .slug(product.getSlug())
                            .price(product.getPrice())
                            .imageUrl(product.getPrimaryImageUrl())
                            .addedAt(item.getAddedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
