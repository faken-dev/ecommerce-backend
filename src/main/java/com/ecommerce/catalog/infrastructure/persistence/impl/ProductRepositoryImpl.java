package com.ecommerce.catalog.infrastructure.persistence.impl;

import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.infrastructure.persistence.mapper.CatalogDomainMapper;
import com.ecommerce.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Optional<Product> findBySlug(String slug) {
        return jpaRepository.findBySlugAndDeletedAtIsNull(slug).map(mapper::toProduct);
    }

    @Override
    public Optional<Product> findBySellerIdAndSlug(UUID sellerId, String slug) {
        return jpaRepository.findActiveBySellerIdAndSlug(sellerId, slug).map(mapper::toProduct);
    }

    @Override
    public Page<Product> search(String query, Collection<UUID> categoryIds, Pageable pageable) {
        return jpaRepository.search(query, categoryIds, pageable).map(mapper::toProduct);
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
            entity.setDeletedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    public long countBySeller(UUID sellerId) {
        return jpaRepository.countBySellerIdAndDeletedAtIsNull(sellerId);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAllByDeletedAtIsNull(pageable).map(mapper::toProduct);
    }

    @Override
    public List<Product> findAllByIds(List<UUID> ids) {
        return jpaRepository.findAllByIdInAndDeletedAtIsNull(ids).stream()
                .map(mapper::toProduct)
                .collect(Collectors.toList());
    }
}
