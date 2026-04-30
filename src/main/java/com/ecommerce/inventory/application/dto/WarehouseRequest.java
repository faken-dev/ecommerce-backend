package com.ecommerce.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    @NotBlank
    private String name;
    private String address;
    
    private boolean active = true;

    public String getName() { return name; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }

    public static WarehouseRequestBuilder builder() { return new WarehouseRequestBuilder(); }
    public static class WarehouseRequestBuilder {
        private String name;
        private String address;
        private boolean active = true;
        public WarehouseRequestBuilder name(String name) { this.name = name; return this; }
        public WarehouseRequestBuilder address(String address) { this.address = address; return this; }
        public WarehouseRequestBuilder active(boolean active) { this.active = active; return this; }
        public WarehouseRequest build() {
            WarehouseRequest r = new WarehouseRequest();
            r.name = name; r.address = address; r.active = active;
            return r;
        }
    }
}
