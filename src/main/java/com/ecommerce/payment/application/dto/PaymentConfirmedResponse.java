package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.entity.PaymentProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedResponse(
    UUID paymentId,
    UUID orderId,
    BigDecimal amount,
    String currency,
    PaymentProvider provider,
    String providerReference,
    Instant paidAt,
    String message
) {}
