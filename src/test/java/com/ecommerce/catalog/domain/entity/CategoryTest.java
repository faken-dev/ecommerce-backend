package com.ecommerce.catalog.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Category Entity — Domain Tests")
class CategoryTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createRoot()")
    class CreateRoot {

        @Test
        @DisplayName("should create root category with correct defaults")
        void createsRoot() {
            Category cat = Fixtures.aRootCategory();

            assertThat(cat.getSlug()).isEqualTo("electronics");
            assertThat(cat.getName()).isEqualTo("Electronics");
            assertThat(cat.getParentId()).isNull();
            assertThat(cat.isActive()).isTrue();
            assertThat(cat.getSortOrder()).isZero();
        }

        @Test
        @DisplayName("should normalise slug to lowercase")
        void normalisesSlug() {
            Category cat = Category.createRoot("ELECTRONICS", "Electronics", "Devices");
            assertThat(cat.getSlug()).isEqualTo("electronics");
        }

        @Test
        @DisplayName("should throw when slug is blank")
        void blankSlug() {
            assertThatThrownBy(() -> Category.createRoot("", "Name", "desc"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when slug exceeds 100 chars")
        void slugTooLong() {
            String longSlug = "a".repeat(101);
            assertThatThrownBy(() -> Category.createRoot(longSlug, "Name", "desc"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when slug has invalid characters")
        void invalidSlug() {
            assertThatThrownBy(() -> Category.createRoot("my category!", "Name", "desc"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("createChild()")
    class CreateChild {

        @Test
        @DisplayName("should create child category with parentId")
        void createsChild() {
            Category child = Fixtures.aChildCategory(Fixtures.CATEGORY_ID);

            assertThat(child.getParentId()).isEqualTo(Fixtures.CATEGORY_ID);
            assertThat(child.getSlug()).isEqualTo("smartphones");
        }
    }

    // ── Domain Methods ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update fields and trim strings")
        void updates() {
            Category cat = Fixtures.aRootCategory();

            cat.update("  New Name  ", "  New desc  ", "https://icon.url", 5);

            assertThat(cat.getName()).isEqualTo("New Name");
            assertThat(cat.getDescription()).isEqualTo("New desc");
            assertThat(cat.getIconUrl()).isEqualTo("https://icon.url");
            assertThat(cat.getSortOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw when name is blank")
        void blankName() {
            Category cat = Fixtures.aRootCategory();
            assertThatThrownBy(() -> cat.update("", "desc", null, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("activate() / deactivate()")
    class ActivateDeactivate {

        @Test
        @DisplayName("activate should set active=true")
        void activates() {
            Category cat = Fixtures.aRootCategory();
            cat.deactivate();
            cat.activate();
            assertThat(cat.isActive()).isTrue();
        }

        @Test
        @DisplayName("deactivate should set active=false")
        void deactivates() {
            Category cat = Fixtures.aRootCategory();
            cat.deactivate();
            assertThat(cat.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateSlug()")
    class UpdateSlug {

        @Test
        @DisplayName("should update slug with validation")
        void updates() {
            Category cat = Fixtures.aRootCategory();
            cat.updateSlug("new-electronics");

            assertThat(cat.getSlug()).isEqualTo("new-electronics");
        }

        @Test
        @DisplayName("should throw for invalid slug")
        void invalidSlug() {
            Category cat = Fixtures.aRootCategory();
            assertThatThrownBy(() -> cat.updateSlug("invalid slug!"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toCreatedEvent contains category id")
        void createdEvent() {
            Category cat = Fixtures.aRootCategory();
            var event = cat.toCreatedEvent();
            assertThat(event.categoryId()).isEqualTo(cat.getId());
        }

        @Test
        @DisplayName("toDeletedEvent contains category id")
        void deletedEvent() {
            Category cat = Fixtures.aRootCategory();
            var event = cat.toDeletedEvent();
            assertThat(event.categoryId()).isEqualTo(cat.getId());
        }
    }
}