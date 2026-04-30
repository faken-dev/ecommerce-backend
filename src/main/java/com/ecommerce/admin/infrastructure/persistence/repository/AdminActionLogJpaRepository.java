package com.ecommerce.admin.infrastructure.persistence.repository;

import com.ecommerce.admin.infrastructure.persistence.entity.AdminActionLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminActionLogJpaRepository extends JpaRepository<AdminActionLogJpaEntity, UUID> {
    List<AdminActionLogJpaEntity> findTop10ByOrderByCreatedAtDesc();
}
