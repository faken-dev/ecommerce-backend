package com.ecommerce.feedback.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record PostCommentRequest(
        @NotBlank String content,
        UUID parentCommentId
) {}
