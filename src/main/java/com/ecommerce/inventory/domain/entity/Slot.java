package com.ecommerce.inventory.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Slot extends AuditableEntity {
    private UUID zoneId;
    private String name;
    private int capacity;

    public Slot() {}

    public static SlotBuilder builder() {
        return new SlotBuilder();
    }

    public static class SlotBuilder {
        private final Slot slot = new Slot();

        public SlotBuilder id(UUID id) { slot.setId(id); return this; }
        public SlotBuilder zoneId(UUID zoneId) { slot.zoneId = zoneId; return this; }
        public SlotBuilder name(String name) { slot.name = name; return this; }
        public SlotBuilder capacity(int capacity) { slot.capacity = capacity; return this; }
        public SlotBuilder createdAt(Instant createdAt) { slot.setCreatedAt(createdAt); return this; }
        public SlotBuilder updatedAt(Instant updatedAt) { slot.setUpdatedAt(updatedAt); return this; }

        public Slot build() {
            return slot;
        }
    }

    public UUID getZoneId() { return zoneId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
}
