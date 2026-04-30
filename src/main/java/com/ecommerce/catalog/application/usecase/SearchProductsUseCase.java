package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final CatalogApplicationMapper mapper;

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> search(String query, int page, int size) {
        return search(query, (UUID) null, page, size);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> search(String query, UUID categoryId, int page, int size) {
        Set<UUID> categoryIds = null;
        if (categoryId != null) {
            categoryIds = new HashSet<>();
            collectCategoryIds(categoryId, categoryIds);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.search(query, categoryIds, pageable);
        List<ProductSummaryResponse> content = enrichSummaries(result.getContent());
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Search results")
                .data(content)
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    private void collectCategoryIds(UUID categoryId, Set<UUID> allIds) {
        if (categoryId == null) return;
        allIds.add(categoryId);
        categoryRepository.findByParentId(categoryId).forEach(child -> 
            collectCategoryIds(child.getId(), allIds)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> byCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.findByCategory(categoryId, pageable);
        List<ProductSummaryResponse> content = enrichSummaries(result.getContent());
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Products by category")
                .data(content)
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> bySeller(UUID sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.findBySeller(sellerId, pageable);
        List<ProductSummaryResponse> content = enrichSummaries(result.getContent());
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Products by seller")
                .data(content)
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> activePublic(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.findActivePublic(pageable);
        List<ProductSummaryResponse> content = enrichSummaries(result.getContent());
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("Active products")
                .data(content)
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProductSummaryResponse>> allForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.findAll(pageable);
        List<ProductSummaryResponse> content = enrichSummaries(result.getContent());
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .success(true)
                .code(200)
                .message("All products (Admin)")
                .data(content)
                .page(ApiResponse.PageMetadata.builder()
                        .size(result.getSize())
                        .number(result.getNumber())
                        .totalElements(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    private List<ProductSummaryResponse> enrichSummaries(List<Product> products) {
        if (products.isEmpty()) return List.of();
        
        List<UUID> productIds = products.stream().map(Product::getId).toList();
        Map<UUID, Integer> stocks = inventoryItemRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        InventoryItem::getProductId,
                        InventoryItem::getQuantity,
                        (v1, v2) -> v1 // In case of duplicate product IDs (shouldn't happen for simple products)
                ));

        Set<UUID> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (UUID id : categoryIds) {
                categoryRepository.findById(id).ifPresent(c -> categoryNames.put(id, c.getName()));
            }
        }

        return products.stream().map(p -> {
            ProductSummaryResponse base = mapper.toProductSummaryResponse(p);
            return new ProductSummaryResponse(
                base.id(), base.name(), base.slug(), base.price(), base.compareAtPrice(),
                stocks.getOrDefault(p.getId(), 0), base.status(), base.isFeatured(), 
                base.averageRating(), base.reviewCount(), base.imageUrl(),
                p.getCategoryId(), categoryNames.getOrDefault(p.getCategoryId(), null)
            );
        }).toList();
    }
}


