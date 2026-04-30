package com.ecommerce.payment.domain.event;

import com.ecommerce.payment.domain.entity.PaymentProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        BigDecimal amount,
        String currency,
        PaymentProvider provider,
        String providerReference,
        Instant paidAt
) {}
