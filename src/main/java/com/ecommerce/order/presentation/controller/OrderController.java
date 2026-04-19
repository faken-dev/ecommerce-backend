package com.ecommerce.order.presentation.controller;

import com.ecommerce.order.application.command.AddToCartCommand;
import com.ecommerce.order.application.command.CancelOrderCommand;
import com.ecommerce.order.application.command.CreateOrderCommand;
import com.ecommerce.order.application.command.UpdateCartItemCommand;
import com.ecommerce.order.application.command.UpdateOrderStatusCommand;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.application.usecase.*;
import com.ecommerce.order.presentation.dto.request.AddToCartRequest;
import com.ecommerce.order.presentation.dto.request.CancelOrderRequest;
import com.ecommerce.order.presentation.dto.request.CreateOrderRequest;
import com.ecommerce.order.presentation.dto.request.UpdateCartItemRequest;
import com.ecommerce.order.presentation.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order and cart management")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final AddToCartUseCase addToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    // CART

    @Operation(summary = "Get current buyer's cart")
    @GetMapping("/cart")
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(getCartUseCase.execute(buyerId)));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/cart/items")
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
    @PutMapping("/cart/items")
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
    @DeleteMapping("/cart/items")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId,
            @AuthenticationPrincipal UUID buyerId) {
        removeCartItemUseCase.execute(productId, variantId, buyerId);
        return ResponseEntity.noContent().build();
    }

    // ORDER — BUYER

    @Operation(summary = "Create order from cart / direct checkout")
    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal UUID buyerId,
            HttpServletRequest httpRequest) {

        String ipAddress = resolveIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        var items = req.items().stream()
                .map(i -> new CreateOrderCommand.OrderItemCommand(
                        i.productId(),
                        i.variantId(),
                        i.quantity(),
                        i.unitPrice(),
                        i.productName(),
                        i.productSku(),
                        i.productImageUrl(),
                        i.variantTitle()))
                .toList();

        var cmd = new CreateOrderCommand(
                req.sellerId(),
                req.shippingAddressId(),
                items,
                req.subtotal(),
                req.shippingFee(),
                req.taxAmount(),
                req.discountAmount(),
                req.currency(),
                req.buyerNote(),
                req.voucherCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(createOrderUseCase.execute(cmd, buyerId,
                        cmd.sellerId(), ipAddress, userAgent)));
    }

    @Operation(summary = "Get order by ID (buyer/seller view)")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getOrderUseCase.execute(orderId, currentUserId, currentUserId)));
    }

    @Operation(summary = "List orders for current buyer")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<Iterable<OrderSummaryResponse>>> listMyOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                listOrdersUseCase.execute(buyerId, status, pageable)));
    }

    @Operation(summary = "Cancel order (buyer)")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('order:cancel')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest req,
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cancelOrderUseCase.execute(
                        new CancelOrderCommand(orderId, req != null ? req.reason() : null),
                        buyerId, false)));
    }

    // ORDER — SELLER / ADMIN

    @Operation(summary = "List orders for seller")
    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<Iterable<OrderSummaryResponse>>> listSellerOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                listOrdersUseCase.executeForSeller(sellerId, status, pageable)));
    }

    @Operation(summary = "Update order status (seller/admin)")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal UUID changedBy,
            @RequestParam String changedByRole) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateOrderStatusUseCase.execute(
                        new UpdateOrderStatusCommand(
                                orderId, req.newStatus(), req.reason(), req.metadata()),
                        changedBy, changedByRole)));
    }

    @Operation(summary = "Cancel order (seller/admin)")
    @PostMapping("/{orderId}/seller-cancel")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> sellerCancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest req,
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cancelOrderUseCase.execute(
                        new CancelOrderCommand(orderId, req != null ? req.reason() : null),
                        sellerId, true)));
    }

    // HELPERS

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}