package com.ecommerce.feedback.presentation.dto.response;

import lombok.Builder;
import java.util.Map;

@Builder
public record RatingSummaryResponse(
    double averageRating,
    long totalReviews,
    Map<Integer, Long> ratingDistribution // 1-5 stars
) {}
