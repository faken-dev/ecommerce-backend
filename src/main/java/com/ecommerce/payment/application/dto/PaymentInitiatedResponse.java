package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.entity.PaymentMethodType;
/**
 * Response returned to the client after a payment is initiated.
 * Contains the redirect URL / deep-link for online payment gateways.
 */
public record PaymentInitiatedResponse(
    String paymentId,
    String orderId,
    String provider,
    PaymentMethodType methodType,
    String status,
    String providerReference,
    String redirectUrl,
    String cancelUrl,
    String message
) {}
