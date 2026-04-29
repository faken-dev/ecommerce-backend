package com.ecommerce.catalog.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com.ecommerce.catalog.domain.event.CategoryCreatedEvent;
import com.ecommerce.catalog.domain.event.CategoryDeletedEvent;
import com.ecommerce.catalog.domain.event.CategoryUpdatedEvent;
import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Category extends AuditableEntity {

    private static final int SLUG_MAX_LENGTH = 100;

    private UUID parentId;
    private String slug;
    private String name;
    private String description;
    private String iconUrl;
    private int sortOrder;
    private boolean active;

    public static Category createRoot(String slug, String name, String description) {
        validateSlug(slug);
        return Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .slug(slug.toLowerCase().trim())
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .sortOrder(0)
                .active(true)
                .build();
    }

    public static Category createChild(String slug, String name, String description, UUID parentId) {
        validateSlug(slug);
        return Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .slug(slug.toLowerCase().trim())
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .parentId(parentId)
                .sortOrder(0)
                .active(true)
                .build();
    }

    private static void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Slug is required");
        }
        String normalised = slug.toLowerCase().trim();
        if (normalised.length() > SLUG_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Slug must not exceed " + SLUG_MAX_LENGTH + " characters");
        }
        if (!normalised.matches("[a-z0-9]+(-[a-z0-9]+)*")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Slug must be lowercase alphanumeric, hyphenated (e.g. electronics, smart-phones)");
        }
    }

    public void update(String name, String description, String iconUrl, int sortOrder) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Name is required");
        }
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.iconUrl = iconUrl != null ? iconUrl.trim() : null;
        this.sortOrder = sortOrder;
        this.touchUpdate();
    }

    public void activate() {
        this.active = true;
        this.touchUpdate();
    }

    public void deactivate() {
        this.active = false;
        this.touchUpdate();
    }

    public void updateSlug(String slug) {
        validateSlug(slug);
        this.slug = slug.toLowerCase().trim();
        this.touchUpdate();
    }

    public CategoryCreatedEvent toCreatedEvent() {
        return new CategoryCreatedEvent(this.getId(), Instant.now());
    }

    public CategoryUpdatedEvent toUpdatedEvent() {
        return new CategoryUpdatedEvent(this.getId(), Instant.now());
    }

    public CategoryDeletedEvent toDeletedEvent() {
        return new CategoryDeletedEvent(this.getId(), Instant.now());
    }
}
