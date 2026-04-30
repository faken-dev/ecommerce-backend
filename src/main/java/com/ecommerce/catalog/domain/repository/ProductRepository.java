package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySellerIdAndSlug(UUID sellerId, String slug);
    Page<Product> search(String query, Collection<UUID> categoryIds, Pageable pageable);
    Page<Product> findByCategory(UUID categoryId, Pageable pageable);
    Page<Product> findBySeller(UUID sellerId, Pageable pageable);
    Page<Product> findActivePublic(Pageable pageable);
    boolean existsBySellerIdAndSlug(UUID sellerId, String slug);
    boolean existsBySellerIdAndSlugExcludingId(UUID sellerId, String slug, UUID excludeId);
    void softDelete(UUID id);
    long countBySeller(UUID sellerId);
    Page<Product> findAll(Pageable pageable);
    List<Product> findAllByIds(List<UUID> ids);
}
