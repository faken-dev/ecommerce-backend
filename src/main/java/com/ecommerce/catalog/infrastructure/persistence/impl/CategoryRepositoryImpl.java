package com.ecommerce.catalog.infrastructure.persistence.impl;

import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ecommerce.catalog.infrastructure.persistence.mapper.CatalogDomainMapper;
import com.ecommerce.catalog.infrastructure.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;
    private final CatalogDomainMapper mapper;

    @Override
    public Category save(Category category) {
        return mapper.toCategory(jpaRepository.save(mapper.toCategoryJpa(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toCategory);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug).map(mapper::toCategory);
    }

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findAll().stream()
                .filter(CategoryJpaEntity::isActive)
                .map(mapper::toCategory)
                .toList();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpaRepository.findAll().stream()
                .filter(e -> parentId.equals(e.getParentId()))
                .map(mapper::toCategory)
                .toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySlugExcludingId(String slug, UUID id) {
        return jpaRepository.existsBySlugAndIdNot(slug, id);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
