package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.application.port.ShippingQueryPort;
import com.ecommerce.order.application.port.UserQueryPort;
import com.ecommerce.order.application.port.PaymentQueryPort;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;
    private final UserQueryPort userQueryPort;
    private final ShippingQueryPort shippingQueryPort;
    private final PaymentQueryPort paymentQueryPort;

    @Transactional(readOnly = true)
    public OrderResponse execute(UUID orderId, UUID buyerId, UUID sellerId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Access control: buyer, seller, or admin only
        boolean isBuyer = order.getBuyerId().equals(buyerId);
        boolean isSeller = order.getSellerId().equals(sellerId);
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isBuyer && !isSeller && !isAdmin) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }

        OrderResponse response = mapper.toOrderResponse(order);
        
        // Enrich with descriptive names
        response.setBuyerName(userQueryPort.getUserFullName(order.getBuyerId()));
        response.setSellerName(userQueryPort.getUserFullName(order.getSellerId()));
        
        // Enrich with payment ID
        response.setPaymentId(paymentQueryPort.getPaymentIdByOrderId(orderId));
        
        // Always try to get shipping address from the port if ID is present
        if (order.getShippingAddressId() != null) {
            response.setShippingAddress(shippingQueryPort.getFullAddress(order.getShippingAddressId()));
        }

        // Set readable payment method name
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
