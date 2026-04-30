package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.CreateProductCommand;
import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProductUseCase")
class CreateProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private CatalogApplicationMapper mapper;
    @Mock private EventPublisher eventPublisher;
    @Mock private com.ecommerce.shared.application.service.MediaAssetService mediaAssetService;

    private CreateProductUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new CreateProductUseCase(productRepository, mapper, eventPublisher, mediaAssetService);
    }

    private CreateProductCommand aCommand() {
        return new CreateProductCommand(
                Fixtures.SELLER_ID,
                "iPhone 15 Pro",
                "iphone-15-pro",
                "Latest Apple flagship",
                new BigDecimal("29990000"),
                null,                   // compareAtPrice
                null,                   // costPerItem
                "SKU-IP15P",            // sku
                "4901234567890",        // barcode
                Fixtures.CATEGORY_ID,   // categoryId
                List.of("apple", "smartphone"),  // tags
                "SHOP",                 // visibility
                "iPhone 15 Pro",        // metaTitle
                "Latest Apple flagship", // metaDescription
                null,                   // weightKg
                null,                   // weightUnit
                null,                   // threeDModelUrl
                List.of()               // images
        );
    }

    private ProductResponse aResponse(UUID productId) {
        return new ProductResponse(
                productId,                  // id
                Fixtures.SELLER_ID,         // sellerId
                "iPhone 15 Pro",            // name
                "iphone-15-pro",            // slug
                "Latest Apple flagship",    // description
                "<p>Latest Apple flagship</p>", // descriptionHtml
                new BigDecimal("29990000"), // price
                null,                       // compareAtPrice
                null,                       // costPerItem
                100,                        // stockQuantity
                10,                         // lowStockThreshold
                "SKU-IP15P",                // sku
                "4901234567890",            // barcode
                Fixtures.CATEGORY_ID,       // categoryId
                "Electronics",              // categoryName
                List.of("apple", "smartphone"), // tags
                "ACTIVE",                   // status
                false,                      // isFeatured
                "SHOP",                     // visibility
                "iPhone 15 Pro",            // metaTitle
                "Latest Apple flagship",    // metaDescription
                BigDecimal.ZERO,            // averageRating
                0,                          // reviewCount
                null,                       // weightKg
                null,                       // weightUnit
                "http://example.com/img.jpg", // imageUrl
                null,                       // threeDModelUrl
                List.of(),                  // images
                Instant.now(),              // createdAt
                Instant.now()               // updatedAt
        );
    }

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should check slug uniqueness before creating")
        void checksUniqueness() {
            when(productRepository.existsBySellerIdAndSlug(
                    Fixtures.SELLER_ID, "iphone-15-pro")).thenReturn(false);
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            sut.execute(aCommand());

            verify(productRepository).existsBySellerIdAndSlug(
                    Fixtures.SELLER_ID, "iphone-15-pro");
        }

        @Test
        @DisplayName("should save product and publish ProductCreatedEvent")
        void savesAndPublishes() {
            when(productRepository.existsBySellerIdAndSlug(any(), any())).thenReturn(false);
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toProductResponse(any())).thenReturn(aResponse(Fixtures.PRODUCT_ID));

            ProductResponse result = sut.execute(aCommand());

            verify(productRepository).save(any(Product.class));
            verify(eventPublisher).publish(any(
                    com.ecommerce.catalog.domain.event.ProductCreatedEvent.class));
            assertThat(result.id()).isEqualTo(Fixtures.PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw PRODUCT_SLUG_ALREADY_EXISTS when slug is taken")
        void slugConflict() {
            when(productRepository.existsBySellerIdAndSlug(
                    Fixtures.SELLER_ID, "iphone-15-pro")).thenReturn(true);

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);

            verify(productRepository, never()).save(any());
        }
    }
}
