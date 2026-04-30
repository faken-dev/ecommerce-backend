package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.OrderSummaryResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.application.port.UserQueryPort;
import com.ecommerce.order.application.port.ShippingQueryPort;
import com.ecommerce.order.application.port.PaymentQueryPort;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;
    private final OrderApplicationMapper mapper;
    private final UserQueryPort userQueryPort;
    private final ShippingQueryPort shippingQueryPort;
    private final PaymentQueryPort paymentQueryPort;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> execute(UUID buyerId, String status, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        } else {
            orders = orderRepository.findByBuyerId(buyerId, pageable);
        }
        return orders.map(this::mapAndEnrichSummary);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> exportAllOrders(String status) {
        Pageable pageable = PageRequest.of(0, 10000);
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.getContent().stream()
                .map(this::mapAndEnrichDetail)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> executeForSeller(UUID sellerId, String status, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        } else {
            orders = orderRepository.findBySellerId(sellerId, pageable);
        }
        return orders.map(this::mapAndEnrichSummary);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> executeForAdmin(String status, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::mapAndEnrichSummary);
    }

    private OrderSummaryResponse mapAndEnrichSummary(Order order) {
        OrderSummaryResponse summary = mapper.toOrderSummaryResponse(order);
        summary.setBuyerName(userQueryPort.getUserFullName(order.getBuyerId()));
        summary.setSellerName(userQueryPort.getUserFullName(order.getSellerId()));
        summary.setPaymentId(paymentQueryPort.getPaymentIdByOrderId(order.getId()));
        return summary;
    }

    private OrderResponse mapAndEnrichDetail(Order order) {
        OrderResponse response = mapper.toOrderResponse(order);
        response.setBuyerName(userQueryPort.getUserFullName(order.getBuyerId()));
        response.setSellerName(userQueryPort.getUserFullName(order.getSellerId()));
        response.setPaymentId(paymentQueryPort.getPaymentIdByOrderId(order.getId()));
        
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
