package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.CreateProductCommand;
import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.catalog.domain.entity.ProductImage;
import com.ecommerce.shared.application.service.MediaAssetService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;
    private final MediaAssetService mediaAssetService;

    @Transactional
    public ProductResponse execute(CreateProductCommand command) {
        if (productRepository.existsBySellerIdAndSlug(command.sellerId(), command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        Product product = Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .sellerId(command.sellerId())
                .name(command.name().trim())
                .slug(command.slug().toLowerCase().trim())
                .description(command.description() != null ? command.description().trim() : null)
                .price(command.price())
                .compareAtPrice(command.compareAtPrice())
                .costPerItem(command.costPerItem())
                .sku(command.sku())
                .barcode(command.barcode())
                .categoryId(command.categoryId())
                .tags(command.tags())
                .status(Product.Status.ACTIVE)
                .isFeatured(false)
                .visibility(Product.Visibility.valueOf(command.visibility() != null ? command.visibility() : "SHOP"))
                .metaTitle(command.metaTitle())
                .metaDescription(command.metaDescription())
                .weightKg(command.weightKg())
                .weightUnit(command.weightUnit() != null ? command.weightUnit() : "KG")
                .threeDModelUrl(command.threeDModelUrl())
                .images(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        // Collect URLs to mark as ACTIVE
        List<String> assetUrls = new ArrayList<>();
        if (command.threeDModelUrl() != null && !command.threeDModelUrl().isBlank()) {
            assetUrls.add(command.threeDModelUrl());
        }
        
        if (command.images() != null) {
            product.setImages(command.images().stream()
                .map(img -> ProductImage.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .url(img.url())
                    .altText(img.altText())
                    .sortOrder(img.sortOrder())
                    .primary(img.primary())
                    .productId(product.getId())
                    .build())
                .collect(Collectors.toList()));
            
            command.images().forEach(img -> assetUrls.add(img.url()));
        }

        // Mark all associated media as ACTIVE
        if (!assetUrls.isEmpty()) {
            mediaAssetService.markAsActive(assetUrls);
        }

        Product saved = productRepository.save(product);
        eventPublisher.publish(saved.toCreatedEvent());
        return mapper.toProductResponse(saved);
    }
}
