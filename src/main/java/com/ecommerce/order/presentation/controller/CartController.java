package com.ecommerce.order.presentation.controller;

import com.ecommerce.order.application.command.AddToCartCommand;
import com.ecommerce.order.application.command.UpdateCartItemCommand;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.usecase.AddToCartUseCase;
import com.ecommerce.order.application.usecase.GetCartUseCase;
import com.ecommerce.order.application.usecase.RemoveCartItemUseCase;
import com.ecommerce.order.application.usecase.UpdateCartItemUseCase;
import com.ecommerce.order.presentation.dto.request.AddToCartRequest;
import com.ecommerce.order.presentation.dto.request.UpdateCartItemRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Cart management controller - separated from OrderController for SRP compliance.
 */
@RestController
@RequestMapping("/api/v1/orders/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping shopping cart management")
public class CartController {

    private final AddToCartUseCase addToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    @Operation(summary = "Get current buyer's cart")
    @GetMapping
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(getCartUseCase.execute(buyerId)));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/items")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest req,
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                addToCartUseCase.execute(
                        new AddToCartCommand(
                                req.productId(),
                                req.variantId(),
                                req.quantity(),
                                req.unitPrice()),
                        buyerId)));
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/items")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest req,
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateCartItemUseCase.execute(
                        new UpdateCartItemCommand(
                                req.productId(),
                                req.variantId(),
                                req.quantity()),
                        buyerId)));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/items")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId,
            @AuthenticationPrincipal UUID buyerId) {
        removeCartItemUseCase.execute(productId, variantId, buyerId);
        return ResponseEntity.noContent().build();
    }
}


