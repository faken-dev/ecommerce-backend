package com.ecommerce.inventory.infrastructure.persistence.impl;

import com.ecommerce.inventory.domain.entity.InventoryLog;
import com.ecommerce.inventory.domain.repository.InventoryLogRepository;
import com.ecommerce.inventory.infrastructure.persistence.mapper.InventoryDomainMapper;
import com.ecommerce.inventory.infrastructure.persistence.repository.InventoryLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryLogRepositoryImpl implements InventoryLogRepository {

    private final InventoryLogJpaRepository jpaRepository;
    private final InventoryDomainMapper mapper;

    @Override
    public void save(InventoryLog log) {
        jpaRepository.save(mapper.toJpa(log));
    }

    @Override
    public List<InventoryLog> findByProductId(UUID productId) {
        return jpaRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
