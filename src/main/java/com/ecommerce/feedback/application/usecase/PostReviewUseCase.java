package com.ecommerce.feedback.application.usecase;

import com.ecommerce.feedback.domain.entity.Review;
import com.ecommerce.feedback.domain.repository.ReviewRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostReviewUseCase {

    private final ReviewRepository repository;

    @Transactional
    public void execute(UUID userId, UUID productId, UUID orderId, int rating, String content, List<String> images) {
        // Verification: Has the user purchased this product?
        // In Shopee, only verified purchasers can leave "Verified" reviews.
        // We can allow all but mark them differently, but here we'll enforce purchase for simplicity.
        if (!repository.hasPurchased(userId, productId)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "You can only review products you have purchased and received.");
        }

        Review review = Review.create(productId, userId, orderId, rating, content);
        if (images != null) {
            images.forEach(review::addImage);
        }

        // For now, auto-approve reviews. In a real app, this would go to PENDING.
        review.approve(); 

        repository.save(review);
    }
}
