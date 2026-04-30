package com.ecommerce.feedback.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReviewComment extends AuditableEntity {

    private UUID reviewId;
    private UUID userId;
    private UUID parentCommentId; // Nullable if top-level reply
    private String content;
    private boolean deleted;

    public ReviewComment() {}

    public static ReviewCommentBuilder create(UUID reviewId, UUID userId, UUID parentCommentId, String content) {
        Instant now = Instant.now();
        return builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .reviewId(reviewId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .content(content)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now);
    }

    public void delete() {
        this.deleted = true;
        touchUpdate();
    }

    public static ReviewCommentBuilder builder() {
        return new ReviewCommentBuilder();
    }

    public static class ReviewCommentBuilder {
        private final ReviewComment comment = new ReviewComment();

        public ReviewCommentBuilder id(UUID id) { comment.setId(id); return this; }
        public ReviewCommentBuilder reviewId(UUID reviewId) { comment.reviewId = reviewId; return this; }
        public ReviewCommentBuilder userId(UUID userId) { comment.userId = userId; return this; }
        public ReviewCommentBuilder parentCommentId(UUID parentCommentId) { comment.parentCommentId = parentCommentId; return this; }
        public ReviewCommentBuilder content(String content) { comment.content = content; return this; }
        public ReviewCommentBuilder deleted(boolean deleted) { comment.deleted = deleted; return this; }
        public ReviewCommentBuilder createdAt(Instant createdAt) { comment.setCreatedAt(createdAt); return this; }
        public ReviewCommentBuilder updatedAt(Instant updatedAt) { comment.setUpdatedAt(updatedAt); return this; }

        public ReviewComment build() {
            return comment;
        }
    }

    public UUID getReviewId() { return reviewId; }
    public UUID getUserId() { return userId; }
    public UUID getParentCommentId() { return parentCommentId; }
    public String getContent() { return content; }
    public boolean isDeleted() { return deleted; }
}
