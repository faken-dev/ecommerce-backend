package com.ecommerce.order.infrastructure.adapter;

import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.order.application.port.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogServiceAdapter implements ProductQueryPort {

    private final ProductRepository productRepository;

    @Override
    public ProductInfo getProductInfo(UUID productId) {
        return productRepository.findById(productId)
                .map(p -> new ProductInfo(
                        p.getId(),
                        p.getSellerId(),
                        p.getName(),
                        p.getPrimaryImageUrl()
                ))
                .orElse(null);
    }
}
