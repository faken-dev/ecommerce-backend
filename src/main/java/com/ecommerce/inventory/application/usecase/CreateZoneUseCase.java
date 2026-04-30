package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.infrastructure.persistence.entity.ZoneJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.ZoneJpaRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateZoneUseCase {

    private final ZoneJpaRepository zoneRepository;

    @Transactional
    public void execute(UUID warehouseId, String name, String description) {
        ZoneJpaEntity zone = ZoneJpaEntity.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .warehouseId(warehouseId)
                .name(name)
                .description(description)
                .build();
        zoneRepository.save(zone);
    }
}
