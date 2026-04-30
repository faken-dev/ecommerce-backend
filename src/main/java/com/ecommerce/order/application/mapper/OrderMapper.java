package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Automated Order Mapper using MapStruct.
 * Eliminates hundreds of lines of manual mapping code.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    @Mapping(target = "itemCount", expression = "java(order.getItems() != null ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() : 0)")
    OrderSummaryResponse toSummaryResponse(Order order);

    List<OrderSummaryResponse> toSummaryList(List<Order> orders);

    OrderResponse.OrderItemResponse toItemResponse(OrderItem item);
    
    OrderResponse.StatusHistoryResponse toStatusHistoryResponse(Order.StatusHistoryEntry entry);
}
