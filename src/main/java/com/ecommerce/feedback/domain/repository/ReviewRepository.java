package com.ecommerce.feedback.domain.repository;

import com.ecommerce.feedback.domain.entity.Review;
import com.ecommerce.feedback.presentation.dto.response.RatingSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(UUID id);
    Page<Review> findByProductId(UUID productId, Pageable pageable);
    boolean hasPurchased(UUID userId, UUID productId);
    RatingSummaryResponse getRatingSummary(UUID productId);
}
