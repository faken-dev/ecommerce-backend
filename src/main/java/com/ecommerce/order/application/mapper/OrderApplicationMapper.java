package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", imports = { BigDecimal.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderApplicationMapper {

    @Mapping(target = "buyerName", ignore = true)
    @Mapping(target = "paymentMethodName", ignore = true)
    @Mapping(target = "sellerName", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    OrderResponse toOrderResponse(Order order);

    OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item);

    @Mapping(target = "fromStatus", source = "fromStatus")
    OrderResponse.StatusHistoryResponse toStatusHistoryResponse(Order.StatusHistoryEntry entry);

    @Mapping(target = "buyerName", ignore = true)
    @Mapping(target = "sellerName", ignore = true)
    @Mapping(target = "itemCount", expression = "java(order.getItems() != null ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() : 0)")
    OrderSummaryResponse toOrderSummaryResponse(Order order);

    List<OrderSummaryResponse> toOrderSummaryList(List<Order> orders);

    @Mapping(target = "itemCount", expression = "java(cart.getItemCount())")
    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "totalAmount", expression = "java(cart.getSubtotal())")
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    CartResponse toCartResponse(Cart cart);

    @Mapping(target = "lineTotal", expression = "java(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))")
    CartResponse.CartItemResponse toCartItemResponse(Cart.CartItem item);
}
