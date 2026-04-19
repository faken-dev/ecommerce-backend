package com.ecommerce.catalog.infrastructure.persistence.impl;

import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.infrastructure.persistence.mapper.CatalogDomainMapper;
import com.ecommerce.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final CatalogDomainMapper mapper;

    @Override
    public Product save(Product product) {
        return mapper.toProduct(jpaRepository.save(mapper.toProductJpa(product)));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(mapper::toProduct);
    }

    @Override
    public Optional<Product> findBySellerIdAndSlug(UUID sellerId, String slug) {
        return jpaRepository.findActiveBySellerIdAndSlug(sellerId, slug).map(mapper::toProduct);
    }

    @Override
    public Page<Product> searchByQuery(String query, Pageable pageable) {
        return jpaRepository.searchByQuery(query, pageable).map(mapper::toProduct);
    }

    @Override
    public Page<Product> findByCategory(UUID categoryId, Pageable pageable) {
        return jpaRepository.findByCategory(categoryId, pageable).map(mapper::toProduct);
    }

    @Override
    public Page<Product> findBySeller(UUID sellerId, Pageable pageable) {
        return jpaRepository.findBySeller(sellerId, pageable).map(mapper::toProduct);
    }

    @Override
    public Page<Product> findActivePublic(Pageable pageable) {
        return jpaRepository.findActivePublic(pageable).map(mapper::toProduct);
    }

    @Override
    public boolean existsBySellerIdAndSlug(UUID sellerId, String slug) {
        return jpaRepository.existsBySellerIdAndSlug(sellerId, slug);
    }

    @Override
    public boolean existsBySellerIdAndSlugExcludingId(UUID sellerId, String slug, UUID excludeId) {
        return jpaRepository.existsBySellerIdAndSlugExcludingId(sellerId, slug, excludeId);
    }

    @Override
    public void softDelete(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(java.time.Instant.now());
            jpaRepository.save(entity);
        });
    }
}
