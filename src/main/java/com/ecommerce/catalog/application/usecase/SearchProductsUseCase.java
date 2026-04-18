package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<ProductSummaryResponse>> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.ecommerce.catalog.domain.entity.Product> result = productRepository.searchByQuery(query, pageable);
        return ApiResponse.<java.util.List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Search results")
                .data(result.getContent().stream().map(mapper::toProductSummaryResponse).toList())
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<ProductSummaryResponse>> byCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.ecommerce.catalog.domain.entity.Product> result = productRepository.findByCategory(categoryId, pageable);
        return ApiResponse.<java.util.List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Products by category")
                .data(result.getContent().stream().map(mapper::toProductSummaryResponse).toList())
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<ProductSummaryResponse>> bySeller(UUID sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.ecommerce.catalog.domain.entity.Product> result = productRepository.findBySeller(sellerId, pageable);
        return ApiResponse.<java.util.List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Products by seller")
                .data(result.getContent().stream().map(mapper::toProductSummaryResponse).toList())
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<ProductSummaryResponse>> activePublic(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.ecommerce.catalog.domain.entity.Product> result = productRepository.findActivePublic(pageable);
        return ApiResponse.<java.util.List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Active products")
                .data(result.getContent().stream().map(mapper::toProductSummaryResponse).toList())
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }
}