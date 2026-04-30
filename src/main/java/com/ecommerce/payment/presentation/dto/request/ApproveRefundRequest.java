package com.ecommerce.payment.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record ApproveRefundRequest(
    @Size(max = 255, message = "Provider refund ID too long")
    String providerRefundId
) {}
