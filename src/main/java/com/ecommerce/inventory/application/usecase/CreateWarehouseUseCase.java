package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.application.dto.WarehouseRequest;
import com.ecommerce.inventory.application.dto.WarehouseResponse;
import com.ecommerce.inventory.infrastructure.persistence.entity.WarehouseJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.WarehouseJpaRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWarehouseUseCase {

    private final WarehouseJpaRepository warehouseRepository;

    @Transactional
    public WarehouseResponse execute(UUID sellerId, WarehouseRequest request) {
        WarehouseJpaEntity jpaEntity = WarehouseJpaEntity.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .sellerId(sellerId)
                .name(request.getName())
                .address(request.getAddress())
                .active(request.isActive())
                .build();

        WarehouseJpaEntity saved = warehouseRepository.save(jpaEntity);

        return WarehouseResponse.builder()
                .id(saved.getId())
                .sellerId(saved.getSellerId())
                .name(saved.getName())
                .address(saved.getAddress())
                .active(saved.isActive())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
