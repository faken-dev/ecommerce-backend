package com.ecommerce.feedback.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        String userName, // We'll need to fetch this or include in entity
        int rating,
        String content,
        List<String> images,
        Instant createdAt,
        List<CommentResponse> replies
) {}
