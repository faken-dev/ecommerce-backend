package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlotJpaRepository extends JpaRepository<SlotJpaEntity, UUID> {
    List<SlotJpaEntity> findByZoneIdAndDeletedAtIsNull(UUID zoneId);
}
