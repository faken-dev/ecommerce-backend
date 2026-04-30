package com.ecommerce.payment.infrastructure.persistence;

import com.ecommerce.order.application.port.PaymentQueryPort;
import com.ecommerce.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentQueryAdapter implements PaymentQueryPort {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public UUID getPaymentIdByOrderId(UUID orderId) {
        // Find the most recent non-failed payment for the order
        return paymentJpaRepository.findFirstByOrderIdAndDeletedAtIsNullOrderByCreatedAtDesc(orderId)
                .map(p -> p.getId())
                .orElse(null);
    }
}
