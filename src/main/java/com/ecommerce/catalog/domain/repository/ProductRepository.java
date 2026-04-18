package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findBySellerIdAndSlug(UUID sellerId, String slug);
    Page<Product> searchByQuery(String query, Pageable pageable);
    Page<Product> findByCategory(UUID categoryId, Pageable pageable);
    Page<Product> findBySeller(UUID sellerId, Pageable pageable);
    Page<Product> findActivePublic(Pageable pageable);
    boolean existsBySellerIdAndSlug(UUID sellerId, String slug);
    boolean existsBySellerIdAndSlugExcludingId(UUID sellerId, String slug, UUID excludeId);
    void softDelete(UUID id);
}
