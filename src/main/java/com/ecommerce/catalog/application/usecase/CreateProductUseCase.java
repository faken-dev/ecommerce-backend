package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.CreateProductCommand;
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
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public ProductResponse execute(CreateProductCommand command) {
        if (productRepository.existsBySellerIdAndSlug(command.sellerId(), command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        Product product = Product.create(command.sellerId(), command.name(), command.slug(), command.description(), command.price())
                .build();
        Product saved = productRepository.save(product);
        eventPublisher.publish(saved.toCreatedEvent());
        return mapper.toProductResponse(saved);
    }
}