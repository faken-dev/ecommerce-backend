package com.ecommerce.inventory.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Warehouse extends AuditableEntity {
    private UUID sellerId;
    private String name;
    private String address;
    private boolean active;

    public Warehouse() {}

    public static WarehouseBuilder builder() {
        return new WarehouseBuilder();
    }

    public static class WarehouseBuilder {
        private final Warehouse warehouse = new Warehouse();

        public WarehouseBuilder id(UUID id) { warehouse.setId(id); return this; }
        public WarehouseBuilder sellerId(UUID sellerId) { warehouse.sellerId = sellerId; return this; }
        public WarehouseBuilder name(String name) { warehouse.name = name; return this; }
        public WarehouseBuilder address(String address) { warehouse.address = address; return this; }
        public WarehouseBuilder active(boolean active) { warehouse.active = active; return this; }
        public WarehouseBuilder createdAt(Instant createdAt) { warehouse.setCreatedAt(createdAt); return this; }
        public WarehouseBuilder updatedAt(Instant updatedAt) { warehouse.setUpdatedAt(updatedAt); return this; }

        public Warehouse build() {
            return warehouse;
        }
    }

    public UUID getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
}
