package com.ecommerce.payment.application.mapper;

import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.domain.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentApplicationMapper {

    public PaymentResponse toResponse(Payment payment) {
        List<PaymentResponse.RefundResponse> refundResponses = payment.getRefunds() != null
                ? payment.getRefunds().stream()
                        .filter(r -> r.getDeletedAt() == null)
                        .map(PaymentResponse.RefundResponse::from)
                        .toList()
                : List.of();

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getBuyerId(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getMaxRefundableAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getMethodType(),
                payment.getProviderReference(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getFailureCode(),
                payment.getDescription(),
                payment.getReturnUrl(),
                payment.getCancelUrl(),
                payment.getPaidAt(),
                payment.getCancelledAt(),
                payment.getCreatedAt(),
                refundResponses
        );
    }
}
