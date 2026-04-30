package com.ecommerce.payment.domain.event;

import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentInitiatedEvent(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        BigDecimal amount,
        String currency,
        PaymentProvider provider,
        PaymentMethodType methodType,
        String providerReference,
        String returnUrl,
        String cancelUrl,
        Instant occurredAt
) {}
