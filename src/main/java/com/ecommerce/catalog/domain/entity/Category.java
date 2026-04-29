package com.ecommerce.catalog.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Category extends AuditableEntity {
    private UUID parentId;
    private String slug;
    private String name;
    private String description;
    private String imageUrl;
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
                .parentId(parentId)
                .slug(slug.toLowerCase().trim())
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .sortOrder(0)
                .active(true)
                .build();
    }

    private static void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BusinessException(ErrorCode.CATALOG_INVALID_SLUG);
        }
    }
}
