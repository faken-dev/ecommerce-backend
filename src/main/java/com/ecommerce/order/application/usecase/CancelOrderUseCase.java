package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.CancelOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(CancelOrderCommand cmd, UUID buyerId, boolean isAdmin) {
        Order order = orderRepository.findByIdWithItems(cmd.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (isAdmin) {
            order.cancel(buyerId);
        } else {
            order.cancelByBuyer(buyerId);
        }

        Order saved = orderRepository.save(order);

        eventPublisher.publish(saved.toCancelledEvent(buyerId));

        return mapper.toOrderResponse(saved);
    }
}
