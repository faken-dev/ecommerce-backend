package com.ecommerce.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record ConfirmPaymentRequest(
    @NotBlank(message = "Provider reference is required")
    @Size(max = 255, message = "Provider reference too long")
    String providerReference,

    String paidAt  // ISO-8601 string - parsed in controller
) {}
