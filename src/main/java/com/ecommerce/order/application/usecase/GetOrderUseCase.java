package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;

    @Transactional(readOnly = true)
    public OrderResponse execute(UUID orderId, UUID buyerId, UUID sellerId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Access control: buyer, seller, or admin only
        boolean isBuyer = order.getBuyerId().equals(buyerId);
        boolean isSeller = order.getSellerId().equals(sellerId);
        boolean isAdmin = false; // check via SecurityContext if needed

        if (!isBuyer && !isSeller && !isAdmin) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }

        return mapper.toOrderResponse(order);
    }
}