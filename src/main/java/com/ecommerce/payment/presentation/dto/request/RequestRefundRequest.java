package com.ecommerce.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RequestRefundRequest(
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be positive")
    BigDecimal amount,

    @Size(max = 1000, message = "Reason too long")
    String reason
) {}
