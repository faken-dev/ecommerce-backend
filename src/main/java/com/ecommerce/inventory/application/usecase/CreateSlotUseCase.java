package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.infrastructure.persistence.entity.SlotJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SlotJpaRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSlotUseCase {

    private final SlotJpaRepository slotRepository;

    @Transactional
    public void execute(UUID zoneId, String name, int capacity) {
        SlotJpaEntity slot = SlotJpaEntity.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .zoneId(zoneId)
                .name(name)
                .capacity(capacity)
                .build();
        slotRepository.save(slot);
    }
}
