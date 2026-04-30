package com.ecommerce.order.application.port;

import java.util.UUID;

public interface ProductQueryPort {
    ProductInfo getProductInfo(UUID productId);

    record ProductInfo(
        UUID id,
        UUID sellerId,
        String name,
        String imageUrl
    ) {}
}
