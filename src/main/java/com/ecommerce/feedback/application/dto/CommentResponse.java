package com.ecommerce.feedback.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        String userName,
        String content,
        Instant createdAt
) {}
