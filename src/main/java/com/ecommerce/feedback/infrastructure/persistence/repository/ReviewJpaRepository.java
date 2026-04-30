package com.ecommerce.feedback.infrastructure.persistence.repository;

import com.ecommerce.feedback.domain.entity.Review.ReviewStatus;
import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, UUID> {
    Page<ReviewJpaEntity> findByProductIdAndStatusOrderByCreatedAtDesc(
            UUID productId, 
            ReviewStatus status, 
            Pageable pageable
    );

    @Query("SELECT r.rating, COUNT(r) FROM ReviewJpaEntity r WHERE r.productId = :productId GROUP BY r.rating")
    List<Object[]> countByRating(UUID productId);

    @Query("SELECT AVG(r.rating) FROM ReviewJpaEntity r")
    Double getAverageRating();
}
