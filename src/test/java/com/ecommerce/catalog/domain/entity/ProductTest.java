package com.ecommerce.catalog.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product Entity — Domain Tests")
class ProductTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create active product with correct defaults")
        void createsActive() {
            Product product = Fixtures.aProduct();

            assertThat(product.getSellerId()).isEqualTo(Fixtures.SELLER_ID);
            assertThat(product.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(product.getSlug()).isEqualTo("iphone-15-pro");
            assertThat(product.getStatus()).isEqualTo(Product.Status.ACTIVE);
            assertThat(product.isFeatured()).isFalse();
            assertThat(product.getVisibility()).isEqualTo(Product.Visibility.SHOP);
        }

        @Test
        @DisplayName("should normalise slug to lowercase")
        void normalisesSlug() {
            Product product = Product.create(
                    Fixtures.SELLER_ID, "My Product", "My-Slug-With-CAPS",
                    "desc", new BigDecimal("100"));

            assertThat(product.getSlug()).isEqualTo("my-slug-with-caps");
        }

        @Test
        @DisplayName("should throw when slug is blank")
        void blankSlug() {
            assertThatThrownBy(() -> Product.create(
                    Fixtures.SELLER_ID, "Name", "", "desc", new BigDecimal("100")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when slug exceeds 350 characters")
        void slugTooLong() {
            String longSlug = "a".repeat(351);
            assertThatThrownBy(() -> Product.create(
                    Fixtures.SELLER_ID, "Name", longSlug, "desc", new BigDecimal("100")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when slug contains invalid characters")
        void invalidSlugChars() {
            assertThatThrownBy(() -> Product.create(
                    Fixtures.SELLER_ID, "Name", "my product!", "desc", new BigDecimal("100")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when price is negative")
        void negativePrice() {
            assertThatThrownBy(() -> Product.create(
                    Fixtures.SELLER_ID, "Name", "valid-slug", "desc",
                    new BigDecimal("-100")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("createAsDraft()")
    class CreateAsDraft {

        @Test
        @DisplayName("should create product with DRAFT status")
        void createsDraft() {
            Product draft = Fixtures.aDraftProduct();
            assertThat(draft.getStatus()).isEqualTo(Product.Status.DRAFT);
        }
    }

    // ── Domain Methods ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update fields and trim strings")
        void updates() {
            Product product = Fixtures.aProduct();

            product.update("  New Name  ", "new-slug", "  New desc  ", new BigDecimal("200"),
                    new BigDecimal("180"), Fixtures.CATEGORY_ID, List.of("tag1"),
                    "Meta Title", "Meta Desc", null);

            assertThat(product.getName()).isEqualTo("New Name");
            assertThat(product.getDescription()).isEqualTo("New desc");
            assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("200"));
            assertThat(product.getCategoryId()).isEqualTo(Fixtures.CATEGORY_ID);
            assertThat(product.getMetaTitle()).isEqualTo("Meta Title");
        }

        @Test
        @DisplayName("should throw when price is negative")
        void negativePrice() {
            Product product = Fixtures.aProduct();
            assertThatThrownBy(() -> product.update("n", "s", "d", new BigDecimal("-1"), null, null, null, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("activate() / archive()")
    class ActivateArchive {

        @Test
        @DisplayName("activate should transition to ACTIVE")
        void activates() {
            Product product = Fixtures.aDraftProduct();
            product.activate();
            assertThat(product.getStatus()).isEqualTo(Product.Status.ACTIVE);
        }

        @Test
        @DisplayName("activate should throw for DELETED product")
        void cannotActivateDeleted() {
            Product product = Fixtures.aProduct();
            product.softDelete();

            assertThatThrownBy(product::activate)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_NOT_ACTIVE);
        }

        @Test
        @DisplayName("archive should transition to ARCHIVED")
        void archives() {
            Product product = Fixtures.aProduct();
            product.archive();
            assertThat(product.getStatus()).isEqualTo(Product.Status.ARCHIVED);
        }
    }

    @Nested
    @DisplayName("softDelete() / restore()")
    class SoftDeleteRestore {

        @Test
        @DisplayName("softDelete should set status to DELETED and set deletedAt")
        void softDeletes() {
            Product product = Fixtures.aProduct();
            product.softDelete();
            assertThat(product.isDeleted()).isTrue();
            assertThat(product.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("restore should reset status to DRAFT and clear deletedAt")
        void restores() {
            Product product = Fixtures.aProduct();
            product.softDelete();
            product.restore();
            assertThat(product.getStatus()).isEqualTo(Product.Status.DRAFT);
            assertThat(product.getDeletedAt()).isNull();
        }
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isAvailable()")
    class Queries {

        @Test
        @DisplayName("isAvailable returns true for ACTIVE non-deleted product")
        void isAvailable() {
            assertThat(Fixtures.aProduct().isAvailable()).isTrue();
        }

        @Test
        @DisplayName("isAvailable returns false for DRAFT")
        void draftNotAvailable() {
            assertThat(Fixtures.aDraftProduct().isAvailable()).isFalse();
        }

        @Test
        @DisplayName("isAvailable returns false when deletedAt is set")
        void deletedNotAvailable() {
            Product product = Fixtures.aProduct();
            product.softDelete();
            assertThat(product.isAvailable()).isFalse();
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toCreatedEvent contains product data")
        void createdEvent() {
            Product product = Fixtures.aProduct();
            var event = product.toCreatedEvent();
            assertThat(event.productId()).isEqualTo(product.getId());
            assertThat(event.sellerId()).isEqualTo(Fixtures.SELLER_ID);
        }

        @Test
        @DisplayName("toDeletedEvent contains product id")
        void deletedEvent() {
            Product product = Fixtures.aProduct();
            var event = product.toDeletedEvent();
            assertThat(event.productId()).isEqualTo(product.getId());
        }
    }
}