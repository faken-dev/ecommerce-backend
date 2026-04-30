package com.ecommerce.shared.infrastructure.persistence.repository;

import com.ecommerce.shared.infrastructure.persistence.entity.MediaAssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAssetJpaEntity, UUID> {
    
    List<MediaAssetJpaEntity> findByUrlIn(Collection<String> urls);

    @Modifying
    @Query("UPDATE MediaAssetJpaEntity m SET m.status = 'ACTIVE' WHERE m.url IN :urls")
    void markAsActive(Collection<String> urls);

    List<MediaAssetJpaEntity> findByStatusAndCreatedAtBefore(String status, Instant timestamp);
}
