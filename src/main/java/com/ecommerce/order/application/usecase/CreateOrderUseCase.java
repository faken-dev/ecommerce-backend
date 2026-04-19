package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(CreateOrderCommand cmd, UUID buyerId, UUID sellerId,
                                 String ipAddress, String userAgent) {

        if (!cmd.sellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN,
                    "sellerId in request does not match authenticated seller");
        }

        List<OrderItem> items = cmd.items().stream()
                .map(itemCmd -> OrderItem.create(
                        null,
                        itemCmd.productId(),
                        itemCmd.productName(),
                        itemCmd.productSku(),
                        itemCmd.productImageUrl(),
                        itemCmd.variantId(),
                        itemCmd.variantTitle(),
                        itemCmd.quantity(),
                        itemCmd.unitPrice()
                ).build())
                .toList();

        BigDecimal discountAmount = cmd.discountAmount() != null
                ? cmd.discountAmount() : BigDecimal.ZERO;

        Order order = Order.createOrder(
                buyerId,
                cmd.sellerId(),
                cmd.shippingAddressId(),
                items,
                cmd.subtotal(),
                cmd.shippingFee(),
                cmd.taxAmount(),
                discountAmount,
                cmd.currency(),
                ipAddress,
                userAgent
        );

        if (cmd.buyerNote() != null && !cmd.buyerNote().isBlank()) {
            order.setBuyerNote(cmd.buyerNote());
        }

        Order saved = orderRepository.save(order);

        eventPublisher.publish(saved.toCreatedEvent());

        // Only delete the cart if one exists for this buyer — a null cart
        // means there is nothing to delete and we should not attempt the delete.
        Cart cart = cartRepository.findByBuyerId(buyerId);
        if (cart != null) {
            cartRepository.delete(cart);
        }

        return mapper.toOrderResponse(saved);
    }
}
