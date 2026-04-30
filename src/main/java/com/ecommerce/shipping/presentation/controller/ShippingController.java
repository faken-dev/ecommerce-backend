package com.ecommerce.shipping.presentation.controller;

import com.ecommerce.shipping.presentation.dto.request.CreateShipmentRequest;
import com.ecommerce.shipping.presentation.dto.request.UpdateShipmentStatusRequest;
import com.ecommerce.shipping.application.dto.CreateShipmentCommand;
import com.ecommerce.shipping.application.dto.ShipmentResponse;
import com.ecommerce.shipping.application.usecase.CreateShipmentUseCase;
import com.ecommerce.shipping.application.usecase.GetShipmentUseCase;
import com.ecommerce.shipping.application.usecase.ListShipmentsUseCase;
import com.ecommerce.shipping.application.usecase.UpdateShipmentStatusUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final CreateShipmentUseCase createShipmentUseCase;
    private final UpdateShipmentStatusUseCase updateShipmentStatusUseCase;
    private final GetShipmentUseCase getShipmentUseCase;
    private final ListShipmentsUseCase listShipmentsUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('shipping:read')")
    public ResponseEntity<Page<ShipmentResponse>> listShipments(Pageable pageable) {
        return ResponseEntity.ok(listShipmentsUseCase.execute(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shipping:read')")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable UUID id) {
        return ResponseEntity.ok(getShipmentUseCase.execute(id));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('shipping:read')")
    public ResponseEntity<ShipmentResponse> getShipmentByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(getShipmentUseCase.findByOrderId(orderId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('shipping:manage')")
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            @RequestParam(required = false) String carrier) {
        
        // Map presentation DTO to application DTO
        CreateShipmentCommand appRequest = new CreateShipmentCommand(
            request.getOrderId(),
            request.getRecipientName(),
            request.getPhone(),
            request.getStreet(),
            request.getDistrict(),
            request.getCity(),
            request.getProvince(),
            request.getCountry(),
            request.getWeight() != null ? BigDecimal.valueOf(request.getWeight()) : null,
            request.getShippingFee() != null ? BigDecimal.valueOf(request.getShippingFee()) : null
        );

        return ResponseEntity.ok(createShipmentUseCase.execute(appRequest, carrier));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('shipping:manage')")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return ResponseEntity.ok(updateShipmentStatusUseCase.execute(id, request.getStatus()));
    }
}
