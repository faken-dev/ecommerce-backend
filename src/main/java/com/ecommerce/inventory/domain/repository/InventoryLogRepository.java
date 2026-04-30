package com.ecommerce.inventory.domain.repository;

import com.ecommerce.inventory.domain.entity.InventoryLog;
import java.util.UUID;
import java.util.List;

public interface InventoryLogRepository {
    void save(InventoryLog log);
    List<InventoryLog> findByProductId(UUID productId);
}
