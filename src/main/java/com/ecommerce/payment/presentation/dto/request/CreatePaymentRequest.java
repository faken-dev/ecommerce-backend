package com.ecommerce.payment.presentation.dto.request;

import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull(message = "Order ID is required")
    UUID orderId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be positive")
    BigDecimal amount,

    @Size(max = 3, message = "Currency code must be 3 characters")
    String currency,

    @NotNull(message = "Provider is required")
    PaymentProvider provider,

    @NotNull(message = "Method type is required")
    PaymentMethodType methodType,

    @Size(max = 500, message = "Description too long")
    String description,

    @Size(max = 255, message = "Idempotency key too long")
    String idempotencyKey,

    @Size(max = 1000, message = "Return URL too long")
    String returnUrl,

    @Size(max = 1000, message = "Cancel URL too long")
    String cancelUrl
) {}
