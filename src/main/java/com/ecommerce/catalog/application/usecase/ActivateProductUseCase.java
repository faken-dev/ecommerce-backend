package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.ActivateProductCommand;
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
public class ActivateProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public ProductResponse execute(ActivateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.activate();
        Product saved = productRepository.save(product);
        eventPublisher.publish(saved.toActivatedEvent());
        return mapper.toProductResponse(saved);
    }
}
