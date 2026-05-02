package com.ecommerce.user.presentation.controller;

import com.ecommerce.user.application.command.CreateAddressCommand;
import com.ecommerce.user.application.command.UpdateAddressCommand;
import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.application.usecase.*;
import com.ecommerce.user.presentation.dto.request.CreateAddressRequest;
import com.ecommerce.user.presentation.dto.request.UpdateAddressRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "User address management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final GetAddressesUseCase getAddressesUseCase;
    private final CreateAddressUseCase createAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final SetDefaultAddressUseCase setDefaultAddressUseCase;

    @GetMapping
    @Operation(summary = "Get all addresses for current user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal(expression = "id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(getAddressesUseCase.execute(userId)));
    }

    @PostMapping
    @Operation(summary = "Create a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody CreateAddressRequest req) {
        AddressResponse address = createAddressUseCase.execute(userId,
                new CreateAddressCommand(
                        req.recipientName(),
                        req.recipientPhone(),
                        req.addressLine(),
                        req.ward(),
                        req.district(),
                        req.province(),
                        Boolean.TRUE.equals(req.isDefault())));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(address, "Address created successfully"));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest req) {
        AddressResponse address = updateAddressUseCase.execute(userId, addressId,
                new UpdateAddressCommand(
                        req.recipientName(),
                        req.recipientPhone(),
                        req.addressLine(),
                        req.ward(),
                        req.district(),
                        req.province()));
        return ResponseEntity.ok(ApiResponse.ok(address, "Address updated successfully"));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID addressId) {
        deleteAddressUseCase.execute(userId, addressId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Address deleted successfully"));
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "Set an address as the default address")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(ApiResponse.ok(
                setDefaultAddressUseCase.execute(userId, addressId),
                "Default address updated successfully"));
    }
}