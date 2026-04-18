package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    Optional<Category> findBySlug(String slug);
    List<Category> findAllActive();
    List<Category> findByParentId(UUID parentId);
    boolean existsBySlug(String slug);
    boolean existsBySlugExcludingId(String slug, UUID id);
    void delete(UUID id);
}
