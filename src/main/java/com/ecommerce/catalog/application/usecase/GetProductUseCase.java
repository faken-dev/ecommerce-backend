package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
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
    private final CategoryRepository categoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final CatalogApplicationMapper mapper;

    @Transactional(readOnly = true)
    public ProductResponse execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return enrich(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse executeByIdentifier(String identifier) {
        Product product;
        try {
            UUID id = UUID.fromString(identifier);
            product = productRepository.findById(id)
                    .orElseGet(() -> productRepository.findBySlug(identifier)
                            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)));
        } catch (IllegalArgumentException e) {
            product = productRepository.findBySlug(identifier)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        }
        return enrich(product);
    }

    private ProductResponse enrich(Product product) {
        int stock = inventoryItemRepository.findByProductId(product.getId())
                .map(InventoryItem::getQuantity)
                .orElse(0);

        ProductResponse response = mapper.toProductResponse(product);
        response = enrichWithStock(response, stock);

        if (product.getCategoryId() != null) {
            String catName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
            return enrichWithCategoryName(response, catName);
        }
        return response;
    }

    private ProductResponse enrichWithStock(ProductResponse base, int stock) {
        return new ProductResponse(
            base.id(), base.sellerId(), base.name(), base.slug(), base.description(),
            base.descriptionHtml(), base.price(), base.compareAtPrice(), base.costPerItem(),
            stock, base.lowStockThreshold(), base.sku(), base.barcode(),
            base.categoryId(), base.categoryName(), base.tags(), base.status(),
            base.isFeatured(), base.visibility(), base.metaTitle(), base.metaDescription(),
            base.averageRating(), base.reviewCount(), base.weightKg(), base.weightUnit(),
            base.imageUrl(), base.threeDModelUrl(), base.images(), base.createdAt(), base.updatedAt()
        );
    }

    private ProductResponse enrichWithCategoryName(ProductResponse base, String categoryName) {
        return new ProductResponse(
            base.id(), base.sellerId(), base.name(), base.slug(), base.description(),
            base.descriptionHtml(), base.price(), base.compareAtPrice(), base.costPerItem(),
            base.stockQuantity(), base.lowStockThreshold(), base.sku(), base.barcode(),
            base.categoryId(), categoryName, base.tags(), base.status(),
            base.isFeatured(), base.visibility(), base.metaTitle(), base.metaDescription(),
            base.averageRating(), base.reviewCount(), base.weightKg(), base.weightUnit(),
            base.imageUrl(), base.threeDModelUrl(), base.images(), base.createdAt(), base.updatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse executeBySellerAndSlug(UUID sellerId, String slug) {
        Product product = productRepository.findBySellerIdAndSlug(sellerId, slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        int stock = inventoryItemRepository.findByProductId(product.getId())
                .map(InventoryItem::getQuantity)
                .orElse(0);

        ProductResponse response = mapper.toProductResponse(product);
        response = enrichWithStock(response, stock);

        if (product.getCategoryId() != null) {
            String catName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
            return enrichWithCategoryName(response, catName);
        }
        return response;
    }
}
