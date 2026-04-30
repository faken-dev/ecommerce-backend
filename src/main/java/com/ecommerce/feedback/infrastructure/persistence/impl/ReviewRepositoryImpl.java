package com.ecommerce.feedback.infrastructure.persistence.impl;

import com.ecommerce.feedback.domain.entity.Review;
import com.ecommerce.feedback.domain.entity.Review.ReviewStatus;
import com.ecommerce.feedback.domain.repository.ReviewRepository;
import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewJpaEntity;
import com.ecommerce.feedback.infrastructure.persistence.mapper.ReviewDomainMapper;
import com.ecommerce.feedback.infrastructure.persistence.repository.ReviewJpaRepository;
import com.ecommerce.feedback.presentation.dto.response.RatingSummaryResponse;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final ReviewDomainMapper mapper;

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = mapper.toJpaEntity(review);
        ReviewJpaEntity saved = reviewJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return reviewJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        return reviewJpaRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                productId, 
                ReviewStatus.APPROVED, 
                pageable
        ).map(mapper::toDomain);
    }

    @Override
    public boolean hasPurchased(UUID userId, UUID productId) {
        return orderJpaRepository.hasPurchasedProduct(userId, productId);
    }

    @Override
    public RatingSummaryResponse getRatingSummary(UUID productId) {
        List<Object[]> results = reviewJpaRepository.countByRating(productId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);

        long total = 0;
        double sum = 0;

        for (Object[] res : results) {
            int rating = (int) res[0];
            long count = (long) res[1];
            distribution.put(rating, count);
            total += count;
            sum += rating * count;
        }

        return RatingSummaryResponse.builder()
                .averageRating(total == 0 ? 0 : sum / total)
                .totalReviews(total)
                .ratingDistribution(distribution)
                .build();
    }
}
