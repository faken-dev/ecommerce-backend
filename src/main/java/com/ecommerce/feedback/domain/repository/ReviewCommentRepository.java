package com.ecommerce.feedback.domain.repository;

import com.ecommerce.feedback.domain.entity.ReviewComment;
import java.util.List;
import java.util.UUID;

public interface ReviewCommentRepository {
    ReviewComment save(ReviewComment comment);
    List<ReviewComment> findByReviewId(UUID reviewId);
}
