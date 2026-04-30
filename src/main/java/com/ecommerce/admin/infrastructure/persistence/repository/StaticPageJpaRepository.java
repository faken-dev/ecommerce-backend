package com.ecommerce.admin.infrastructure.persistence.repository;

import com.ecommerce.admin.infrastructure.persistence.entity.StaticPageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaticPageJpaRepository extends JpaRepository<StaticPageJpaEntity, UUID> {
    Optional<StaticPageJpaEntity> findBySlug(String slug);
}
