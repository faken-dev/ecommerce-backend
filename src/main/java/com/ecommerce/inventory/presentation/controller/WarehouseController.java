package com.ecommerce.inventory.presentation.controller;

import com.ecommerce.inventory.application.dto.WarehouseRequest;
import com.ecommerce.inventory.application.dto.WarehouseResponse;
import com.ecommerce.inventory.application.usecase.*;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "Endpoints for sellers to manage warehouses, zones, and slots")
@SecurityRequirement(name = "bearerAuth")
public class WarehouseController {

    private final GetSellerWarehousesUseCase getSellerWarehousesUseCase;
    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final GetWarehouseStructureUseCase getWarehouseStructureUseCase;
    private final CreateZoneUseCase createZoneUseCase;
    private final CreateSlotUseCase createSlotUseCase;

    @GetMapping
    @Operation(summary = "Get all warehouses for the current seller")
    @PreAuthorize("hasAuthority('inventory:manage')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getMyWarehouses(
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(getSellerWarehousesUseCase.execute(sellerId)));
    }

    @PostMapping
    @Operation(summary = "Create a new warehouse")
    @PreAuthorize("hasAuthority('inventory:manage')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @AuthenticationPrincipal UUID sellerId,
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(createWarehouseUseCase.execute(sellerId, request)));
    }

    @GetMapping("/{warehouseId}/structure")
    @Operation(summary = "Get the full structure (zones/slots) of a warehouse")
    @PreAuthorize("hasAuthority('inventory:manage')")
    public ResponseEntity<ApiResponse<List<GetWarehouseStructureUseCase.ZoneStructure>>> getStructure(
            @PathVariable UUID warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(getWarehouseStructureUseCase.execute(warehouseId)));
    }

    @PostMapping("/{warehouseId}/zones")
    @Operation(summary = "Add a new zone to a warehouse")
    @PreAuthorize("hasAuthority('inventory:manage')")
    public ResponseEntity<ApiResponse<Void>> addZone(
            @PathVariable UUID warehouseId,
            @RequestBody Map<String, String> req) {
        createZoneUseCase.execute(warehouseId, req.get("name"), req.get("description"));
        return ResponseEntity.ok(ApiResponse.success("Zone added successfully"));
    }

    @PostMapping("/zones/{zoneId}/slots")
    @Operation(summary = "Add a new slot to a zone")
    @PreAuthorize("hasAuthority('inventory:manage')")
    public ResponseEntity<ApiResponse<Void>> addSlot(
            @PathVariable UUID zoneId,
            @RequestBody Map<String, Object> req) {
        createSlotUseCase.execute(zoneId, (String) req.get("name"), (Integer) req.get("capacity"));
        return ResponseEntity.ok(ApiResponse.success("Slot added successfully"));
    }
}


