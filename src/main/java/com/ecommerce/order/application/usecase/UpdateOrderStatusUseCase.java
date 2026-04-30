package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.UpdateOrderStatusCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.application.port.UserQueryPort;
import com.ecommerce.order.application.port.ShippingQueryPort;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.audit.domain.annotation.Audited;
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
    private final UserQueryPort userQueryPort;
    private final ShippingQueryPort shippingQueryPort;

    @Audited(action = "UPDATE_ORDER_STATUS", resource = "ORDER")
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

        return enrichResponse(mapper.toOrderResponse(saved), saved);
    }

    private OrderResponse enrichResponse(OrderResponse response, Order order) {
        response.setBuyerName(userQueryPort.getUserFullName(order.getBuyerId()));
        response.setSellerName(userQueryPort.getUserFullName(order.getSellerId()));
        
        if (order.getShippingAddressId() != null) {
            response.setShippingAddress(shippingQueryPort.getFullAddress(order.getShippingAddressId()));
        }

        String pm = order.getPaymentMethod();
        if (pm != null) {
            response.setPaymentMethodName(formatPaymentMethod(pm));
        }
        return response;
    }

    private String formatPaymentMethod(String pm) {
        if (pm.equalsIgnoreCase("COD")) return "Cash on Delivery";
        if (pm.equalsIgnoreCase("ZALOPAY")) return "ZaloPay Wallet";
        if (pm.equalsIgnoreCase("VNPAY")) return "VNPay";
        if (pm.equalsIgnoreCase("STRIPE")) return "Credit/Debit Card (Stripe)";
        return pm;
    }
}
