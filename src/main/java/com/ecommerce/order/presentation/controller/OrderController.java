package com.ecommerce.order.presentation.controller;

import com.ecommerce.order.application.command.CancelOrderCommand;
import com.ecommerce.order.application.command.CreateOrderCommand;
import com.ecommerce.order.application.command.UpdateOrderStatusCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.application.usecase.*;
import com.ecommerce.order.presentation.dto.request.CancelOrderRequest;
import com.ecommerce.order.presentation.dto.request.CreateOrderRequest;
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
    private final DeleteOrderUseCase deleteOrderUseCase;

    @Operation(summary = "Create order from cart / direct checkout")
    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal(expression = "id") UUID buyerId,
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
                req.voucherCode(),
                req.paymentMethod());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(createOrderUseCase.execute(cmd, buyerId, ipAddress, userAgent)));
    }

    @Operation(summary = "Get order by ID (buyer/seller view)")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal(expression = "id") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getOrderUseCase.execute(orderId, currentUserId, currentUserId)));
    }

    @Operation(summary = "List orders for current buyer")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('order:read')")
    public ResponseEntity<ApiResponse<Iterable<OrderSummaryResponse>>> listMyOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal(expression = "id") UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                listOrdersUseCase.execute(buyerId, status, pageable)));
    }

    @Operation(summary = "Cancel order (buyer)")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('order:cancel')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest req,
            @AuthenticationPrincipal(expression = "id") UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cancelOrderUseCase.execute(
                        new CancelOrderCommand(orderId, req != null ? req.reason() : null),
                        buyerId, false)));
    }

    @Operation(summary = "List orders for seller")
    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<Iterable<OrderSummaryResponse>>> listSellerOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal(expression = "id") UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                listOrdersUseCase.executeForSeller(sellerId, status, pageable)));
    }

    @Operation(summary = "Update order status (seller/admin)")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal(expression = "id") UUID changedBy,
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
            @AuthenticationPrincipal(expression = "id") UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cancelOrderUseCase.execute(
                        new CancelOrderCommand(orderId, req != null ? req.reason() : null),
                        sellerId, true)));
    }

    @Operation(summary = "List all orders (admin)")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<Iterable<OrderSummaryResponse>>> listAllOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                listOrdersUseCase.executeForAdmin(status, pageable)));
    }

    @Operation(summary = "Get order by ID (admin view)")
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> getAdminOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal(expression = "id") UUID adminId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getOrderUseCase.execute(orderId, adminId, null))); 
    }

    @Operation(summary = "Update order status (admin)")
    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateAdminOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal(expression = "id") UUID adminId) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateOrderStatusUseCase.execute(
                        new UpdateOrderStatusCommand(
                                orderId, req.newStatus(), req.reason(), req.metadata()),
                        adminId, "ADMIN")));
    }

    @Operation(summary = "Cancel order (admin)")
    @PostMapping("/admin/{orderId}/cancel")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<OrderResponse>> adminCancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest req,
            @AuthenticationPrincipal(expression = "id") UUID adminId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cancelOrderUseCase.execute(
                        new CancelOrderCommand(orderId, req != null ? req.reason() : null),
                        adminId, true)));
    }

    @Operation(summary = "Delete order (admin)")
    @DeleteMapping("/admin/{orderId}")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID orderId) {
        deleteOrderUseCase.execute(orderId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Order deleted successfully"));
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}


