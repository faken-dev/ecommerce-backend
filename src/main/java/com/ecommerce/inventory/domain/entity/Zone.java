package com.ecommerce.inventory.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Zone extends AuditableEntity {
    private UUID warehouseId;
    private String name;
    private String description;

    public Zone() {}

    public static ZoneBuilder builder() {
        return new ZoneBuilder();
    }

    public static class ZoneBuilder {
        private final Zone zone = new Zone();

        public ZoneBuilder id(UUID id) { zone.setId(id); return this; }
        public ZoneBuilder warehouseId(UUID warehouseId) { zone.warehouseId = warehouseId; return this; }
        public ZoneBuilder name(String name) { zone.name = name; return this; }
        public ZoneBuilder description(String description) { zone.description = description; return this; }
        public ZoneBuilder createdAt(Instant createdAt) { zone.setCreatedAt(createdAt); return this; }
        public ZoneBuilder updatedAt(Instant updatedAt) { zone.setUpdatedAt(updatedAt); return this; }

        public Zone build() {
            return zone;
        }
    }

    public UUID getWarehouseId() { return warehouseId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
