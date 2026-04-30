package com.ecommerce.order.application.port;

import java.util.UUID;

public interface InventoryQueryPort {
    int getAvailableStock(UUID productId, UUID variantId);
    long countLowStock();
}
