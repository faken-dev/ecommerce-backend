package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.AdjustStockCommand;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdjustStockUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(AdjustStockCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        int newStock = product.getStockQuantity() + command.adjustment();
        if (newStock < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK,
                    "Cannot reduce stock below 0. Current: " + product.getStockQuantity() +
                    ", requested: " + command.adjustment());
        }

        product.adjustStock(command.adjustment());
        productRepository.save(product);
    }
}