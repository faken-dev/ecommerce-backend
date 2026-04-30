package com.ecommerce.admin.infrastructure.persistence.repository;

import com.ecommerce.admin.infrastructure.persistence.entity.BannerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerJpaRepository extends JpaRepository<BannerJpaEntity, UUID> {
    List<BannerJpaEntity> findByStatusOrderByPriorityDesc(String status);
}
