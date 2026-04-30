package com.ecommerce.feedback.infrastructure.persistence.repository;

import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewCommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewCommentJpaRepository extends JpaRepository<ReviewCommentJpaEntity, UUID> {
    List<ReviewCommentJpaEntity> findByReviewIdOrderByCreatedAtAsc(UUID reviewId);
}
