package com.ecommerce.inventory.presentation.controller;

import java.util.Map;
import org.springframework.data.domain.Pageable;
import com.ecommerce.inventory.application.usecase.ListInventoryLogsUseCase;
import com.ecommerce.inventory.application.usecase.UpdateProductLocationUseCase;
import com.ecommerce.inventory.application.usecase.UpdateStockUseCase;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryItemRepository;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management")
public class InventoryController {

    private final InventoryItemRepository inventoryItemRepository;
    private final UpdateStockUseCase updateStockUseCase;
    private final ListInventoryLogsUseCase listInventoryLogsUseCase;
    private final UpdateProductLocationUseCase updateProductLocationUseCase;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock for a product (public)")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getStock(@PathVariable UUID productId) {
        return inventoryItemRepository.findByProductId(productId)
                .map(item -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(item))))
                .orElse(ResponseEntity.ok(ApiResponse.ok(InventoryItemResponse.builder()
                        .productId(productId)
                        .quantity(0)
                        .reservedQuantity(0)
                        .availableQuantity(0)
                        .build())));
    }

    @PostMapping("/admin/adjust")
    @PreAuthorize("hasAuthority('inventory:manage')")
    @Operation(summary = "Adjust stock (admin)")
    public ResponseEntity<ApiResponse<Void>> adjustStock(@RequestBody AdjustStockRequest req) {
        updateStockUseCase.execute(req.productId(), req.variantId(), req.change(), req.action(), req.reason());
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully"));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('inventory:manage')")
    @Operation(summary = "List all inventory items (paginated)")
    public ResponseEntity<ApiResponse<Iterable<InventoryItemResponse>>> listAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryItemRepository.findAll(pageable).map(this::mapToResponse)));
    }

    @GetMapping("/seller/all")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "List all inventory items for current seller (paginated)")
    public ResponseEntity<ApiResponse<Iterable<InventoryItemResponse>>> listMyInventory(
            @AuthenticationPrincipal UUID sellerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryItemRepository.findAllBySellerId(sellerId, pageable).map(this::mapToResponse)));
    }

    @PutMapping("/{productId}/location")
    @PreAuthorize("hasAuthority('inventory:manage')")
    @Operation(summary = "Update product storage location (slot)")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable UUID productId,
            @RequestBody Map<String, UUID> req) {
        updateProductLocationUseCase.execute(productId, req.get("slotId"));
        return ResponseEntity.ok(ApiResponse.success("Location updated"));
    }

    @GetMapping("/admin/logs/{productId}")
    @PreAuthorize("hasAuthority('inventory:manage')")
    @Operation(summary = "Get stock movement logs for a product")
    public ResponseEntity<ApiResponse<List<ListInventoryLogsUseCase.InventoryLogResponse>>> getLogs(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(listInventoryLogsUseCase.execute(productId)));
    }

    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .slotId(item.getSlotId())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getAvailableQuantity())
                .build();
    }

    public static class InventoryItemResponse {
        private UUID id;
        private UUID productId;
        private UUID variantId;
        private UUID slotId;
        private int quantity;
        private int reservedQuantity;
        private int availableQuantity;

        public InventoryItemResponse() {}

        public InventoryItemResponse(UUID id, UUID productId, UUID variantId, UUID slotId, int quantity, int reservedQuantity, int availableQuantity) {
            this.id = id;
            this.productId = productId;
            this.variantId = variantId;
            this.slotId = slotId;
            this.quantity = quantity;
            this.reservedQuantity = reservedQuantity;
            this.availableQuantity = availableQuantity;
        }

        public UUID getId() { return id; }
        public UUID getProductId() { return productId; }
        public UUID getVariantId() { return variantId; }
        public UUID getSlotId() { return slotId; }
        public int getQuantity() { return quantity; }
        public int getReservedQuantity() { return reservedQuantity; }
        public int getAvailableQuantity() { return availableQuantity; }

        public static InventoryItemResponseBuilder builder() {
            return new InventoryItemResponseBuilder();
        }

        public static class InventoryItemResponseBuilder {
            private UUID id;
            private UUID productId;
            private UUID variantId;
            private UUID slotId;
            private int quantity;
            private int reservedQuantity;
            private int availableQuantity;

            public InventoryItemResponseBuilder id(UUID id) { this.id = id; return this; }
            public InventoryItemResponseBuilder productId(UUID productId) { this.productId = productId; return this; }
            public InventoryItemResponseBuilder variantId(UUID variantId) { this.variantId = variantId; return this; }
            public InventoryItemResponseBuilder slotId(UUID slotId) { this.slotId = slotId; return this; }
            public InventoryItemResponseBuilder quantity(int quantity) { this.quantity = quantity; return this; }
            public InventoryItemResponseBuilder reservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; return this; }
            public InventoryItemResponseBuilder availableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; return this; }

            public InventoryItemResponse build() {
                return new InventoryItemResponse(id, productId, variantId, slotId, quantity, reservedQuantity, availableQuantity);
            }
        }
    }

    public record AdjustStockRequest(
            UUID productId,
            UUID variantId,
            int change,
            String action,
            String reason
    ) {}
}


