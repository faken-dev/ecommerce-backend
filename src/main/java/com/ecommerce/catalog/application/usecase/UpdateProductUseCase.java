package com.ecommerce.catalog.application.usecase;

import com.ecommerce.audit.domain.annotation.Audited;
import com.ecommerce.catalog.application.command.UpdateProductCommand;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;
    private final MediaAssetService mediaAssetService;

    @Audited(action = "UPDATE_PRODUCT", resource = "PRODUCT")
    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
        Product product = productRepository.findById(command.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (productRepository.existsBySellerIdAndSlugExcludingId(
                product.getSellerId(), command.slug(), command.id())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        product.update(command.name(), command.slug(), command.description(), command.price(),
                command.compareAtPrice(), command.categoryId(), command.tags(),
                command.metaTitle(), command.metaDescription(), command.threeDModelUrl());
        
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

        if (!assetUrls.isEmpty()) {
            mediaAssetService.markAsActive(assetUrls);
        }

        Product saved = productRepository.save(product);
        eventPublisher.publish(saved.toUpdatedEvent());
        return mapper.toProductResponse(saved);
    }
}
