package com.ecommerce.payment.application.command;

import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to initiate a new payment for an order.
 */
public record CreatePaymentCommand(
    UUID orderId,
    UUID buyerId,
    BigDecimal amount,
    String currency,
    PaymentProvider provider,
    PaymentMethodType methodType,
    String description,
    String idempotencyKey,
    String returnUrl,
    String cancelUrl,
    String ipAddress,
    String userAgent
) {}
