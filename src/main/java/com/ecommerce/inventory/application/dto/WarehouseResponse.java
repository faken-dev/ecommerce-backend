package com.ecommerce.inventory.application.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class WarehouseResponse {
    private UUID id;
    private UUID sellerId;
    private String name;
    private String address;
    private boolean active;
    private Instant createdAt;

    public WarehouseResponse() {}

    public WarehouseResponse(UUID id, UUID sellerId, String name, String address, boolean active, Instant createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public static WarehouseResponseBuilder builder() { return new WarehouseResponseBuilder(); }
    public static class WarehouseResponseBuilder {
        private UUID id;
        private UUID sellerId;
        private String name;
        private String address;
        private boolean active;
        private Instant createdAt;
        public WarehouseResponseBuilder id(UUID id) { this.id = id; return this; }
        public WarehouseResponseBuilder sellerId(UUID sellerId) { this.sellerId = sellerId; return this; }
        public WarehouseResponseBuilder name(String name) { this.name = name; return this; }
        public WarehouseResponseBuilder address(String address) { this.address = address; return this; }
        public WarehouseResponseBuilder active(boolean active) { this.active = active; return this; }
        public WarehouseResponseBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public WarehouseResponse build() {
            return new WarehouseResponse(id, sellerId, name, address, active, createdAt);
        }
    }
}
