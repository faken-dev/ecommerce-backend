package com.ecommerce.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRefundRequest(
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 1000, message = "Rejection reason too long")
    String reason
) {}
