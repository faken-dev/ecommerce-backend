package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.application.dto.WarehouseResponse;
import com.ecommerce.inventory.infrastructure.persistence.repository.WarehouseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetSellerWarehousesUseCase {

    private final WarehouseJpaRepository warehouseRepository;

    @Transactional(readOnly = true)
    public List<WarehouseResponse> execute(UUID sellerId) {
        return warehouseRepository.findBySellerIdAndDeletedAtIsNull(sellerId).stream()
                .map(w -> WarehouseResponse.builder()
                        .id(w.getId())
                        .name(w.getName())
                        .address(w.getAddress())
                        .active(w.isActive())
                        .createdAt(w.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
