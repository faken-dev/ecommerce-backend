package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.UpdateOrderStatusCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
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
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(UpdateOrderStatusCommand cmd, UUID changedBy, String changedByRole) {
        Order order = orderRepository.findByIdWithItems(cmd.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus previousStatus = order.getStatus();
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(cmd.newStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "Unknown status: " + cmd.newStatus());
        }

        order.transitionTo(newStatus);
        orderRepository.saveStatusHistory(
                order.getId(),
                previousStatus.name(),
                newStatus.name(),
                changedBy,
                changedByRole,
                cmd.reason(),
                cmd.metadata()
        );

        Order saved = orderRepository.save(order);

        eventPublisher.publish(saved.toStatusChangedEvent(previousStatus, newStatus));

        return mapper.toOrderResponse(saved);
    }
}
