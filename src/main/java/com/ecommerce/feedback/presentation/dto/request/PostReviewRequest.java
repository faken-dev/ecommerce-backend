package com.ecommerce.feedback.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PostReviewRequest(
        @NotNull UUID productId,
        UUID orderId,
        @Min(1) @Max(5) int rating,
        @NotBlank String content,
        List<String> images
) {}
