package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> execute(UUID buyerId, String status, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        } else {
            orders = orderRepository.findByBuyerId(buyerId, pageable);
        }
        return orders.map(mapper::toOrderSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> executeForSeller(UUID sellerId, String status, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        } else {
            orders = orderRepository.findBySellerId(sellerId, pageable);
        }
        return orders.map(mapper::toOrderSummaryResponse);
    }
}