package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.UpdateProductCommand;
import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
        Product product = productRepository.findById(command.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (productRepository.existsBySellerIdAndSlugExcludingId(
                product.getSellerId(), command.slug(), command.id())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        product.update(command.name(), command.description(), command.price(),
                command.compareAtPrice(), command.categoryId(), command.tags(),
                command.metaTitle(), command.metaDescription());
        Product saved = productRepository.save(product);
        eventPublisher.publish(saved.toUpdatedEvent());
        return mapper.toProductResponse(saved);
    }
}