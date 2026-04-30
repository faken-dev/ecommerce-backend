package com.ecommerce.feedback.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Review extends AuditableEntity {

    private UUID productId;
    private UUID userId;
    private UUID orderId; // Nullable if not from a specific order
    private int rating;
    private String content;
    private List<String> images = new ArrayList<>();
    private ReviewStatus status;

    public Review() {}

    public enum ReviewStatus {
        PENDING, APPROVED, HIDDEN
    }

    public static Review create(UUID productId, UUID userId, UUID orderId, int rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Rating must be between 1 and 5");
        }

        Instant now = Instant.now();
        return builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .productId(productId)
                .userId(userId)
                .orderId(orderId)
                .rating(rating)
                .content(content)
                .status(ReviewStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void approve() {
        this.status = ReviewStatus.APPROVED;
        touchUpdate();
    }

    public void hide() {
        this.status = ReviewStatus.HIDDEN;
        touchUpdate();
    }

    public void addImage(String url) {
        this.images.add(url);
    }

    public static ReviewBuilder builder() {
        return new ReviewBuilder();
    }

    public static class ReviewBuilder {
        private final Review review = new Review();

        public ReviewBuilder id(UUID id) { review.setId(id); return this; }
        public ReviewBuilder productId(UUID productId) { review.productId = productId; return this; }
        public ReviewBuilder userId(UUID userId) { review.userId = userId; return this; }
        public ReviewBuilder orderId(UUID orderId) { review.orderId = orderId; return this; }
        public ReviewBuilder rating(int rating) { review.rating = rating; return this; }
        public ReviewBuilder content(String content) { review.content = content; return this; }
        public ReviewBuilder images(List<String> images) { review.images = images; return this; }
        public ReviewBuilder status(ReviewStatus status) { review.status = status; return this; }
        public ReviewBuilder createdAt(Instant createdAt) { review.setCreatedAt(createdAt); return this; }
        public ReviewBuilder updatedAt(Instant updatedAt) { review.setUpdatedAt(updatedAt); return this; }

        public Review build() {
            return review;
        }
    }

    public UUID getProductId() { return productId; }
    public UUID getUserId() { return userId; }
    public UUID getOrderId() { return orderId; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public List<String> getImages() { return images; }
    public ReviewStatus getStatus() { return status; }
}
