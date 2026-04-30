package com.ecommerce.catalog.infrastructure.persistence.specification;

import com.ecommerce.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ecommerce.catalog.presentation.dto.request.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<ProductJpaEntity> withCriteria(ProductSearchCriteria criteria) {
        Specification<ProductJpaEntity> spec = Specification.where(isNotDeleted());
        
        if (criteria == null) return spec;

        spec = spec.and(hasKeyword(criteria.query()))
                   .and(hasCategory(criteria.categoryId()))
                   .and(hasPriceBetween(criteria.minPrice(), criteria.maxPrice()));

        if (Boolean.TRUE.equals(criteria.inStock())) {
             spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0));
        }

        return spec;
    }

    public static Specification<ProductJpaEntity> hasKeyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<ProductJpaEntity> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<ProductJpaEntity> hasCategories(Collection<UUID> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return null;
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<ProductJpaEntity> hasPriceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    public static Specification<ProductJpaEntity> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<ProductJpaEntity> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}
