package com.ecommerce.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record FailPaymentRequest(
    @Size(max = 50, message = "Failure code too long")
    String failureCode,

    @NotBlank(message = "Failure reason is required")
    @Size(max = 500, message = "Failure reason too long")
    String failureReason
) {}
