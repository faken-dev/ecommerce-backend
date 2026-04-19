package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.*;
import com.ecommerce.order.domain.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps between Order domain entities and application DTOs/Commands.
 * Manual implementation (not MapStruct) for flexibility.
 */
@Component
public class OrderApplicationMapper {

    // ── Order → Response ───────────────────────────────────────────────────

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .status(order.getStatus().name())
                .shippingAddressId(order.getShippingAddressId())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus().name())
                .buyerNote(order.getBuyerNote())
                .sellerNote(order.getSellerNote())
                .shippingCarrier(order.getShippingCarrier())
                .trackingNumber(order.getTrackingNumber())
                .cancelWindowSec(order.getCancelWindowSec())
                .buyerCancellable(order.isBuyerCancellable())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() != null
                        ? order.getItems().stream().map(this::toOrderItemResponse).toList()
                        : List.of())
                .statusHistory(order.getStatusHistory() != null
                        ? order.getStatusHistory().stream().map(this::toStatusHistoryResponse).toList()
                        : List.of())
                .build();
    }

    public OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) return null;
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImageUrl(item.getProductImageUrl())
                .variantId(item.getVariantId())
                .variantTitle(item.getVariantTitle())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .discountAmount(item.getDiscountAmount())
                .refundedQuantity(item.getRefundedQuantity())
                .refundedAmount(item.getRefundedAmount())
                .build();
    }

    public OrderResponse.StatusHistoryResponse toStatusHistoryResponse(Order.StatusHistoryEntry entry) {
        if (entry == null) return null;
        return OrderResponse.StatusHistoryResponse.builder()
                .id(entry.id())
                .fromStatus(entry.fromStatus() != null ? entry.fromStatus().name() : null)
                .toStatus(entry.toStatus().name())
                .changedBy(entry.changedBy())
                .changedByRole(entry.changedByRole())
                .reason(entry.reason())
                .createdAt(entry.createdAt())
                .build();
    }

    public OrderSummaryResponse toOrderSummaryResponse(Order order) {
        if (order == null) return null;
        int itemCount = order.getItems() != null
                ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
                : 0;
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentStatus(order.getPaymentStatus().name())
                .itemCount(itemCount)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderSummaryResponse> toOrderSummaryList(List<Order> orders) {
        if (orders == null) return List.of();
        return orders.stream().map(this::toOrderSummaryResponse).toList();
    }

    // ── Order → Cart Response ──────────────────────────────────────────────

    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) return null;
        return CartResponse.builder()
                .id(cart.getId())
                .buyerId(cart.getBuyerId())
                .itemCount(cart.getItemCount())
                .subtotal(cart.getSubtotal())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(cart.getItems() != null
                        ? cart.getItems().stream().map(this::toCartItemResponse).toList()
                        : List.of())
                .build();
    }

    private CartResponse.CartItemResponse toCartItemResponse(Cart.CartItem item) {
        if (item == null) return null;
        return CartResponse.CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .addedAt(item.getAddedAt())
                .build();
    }
}