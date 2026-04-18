package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;

    @Transactional(readOnly = true)
    public ProductResponse execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return mapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse executeBySellerAndSlug(UUID sellerId, String slug) {
        Product product = productRepository.findBySellerIdAndSlug(sellerId, slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return mapper.toProductResponse(product);
    }
}