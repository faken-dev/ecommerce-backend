package com.ecommerce.feedback.infrastructure.persistence.impl;

import com.ecommerce.feedback.domain.entity.ReviewComment;
import com.ecommerce.feedback.domain.repository.ReviewCommentRepository;
import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewCommentJpaEntity;
import com.ecommerce.feedback.infrastructure.persistence.mapper.ReviewCommentDomainMapper;
import com.ecommerce.feedback.infrastructure.persistence.repository.ReviewCommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewCommentRepositoryImpl implements ReviewCommentRepository {

    private final ReviewCommentJpaRepository jpaRepository;
    private final ReviewCommentDomainMapper mapper;

    @Override
    public ReviewComment save(ReviewComment comment) {
        ReviewCommentJpaEntity entity = mapper.toJpaEntity(comment);
        ReviewCommentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<ReviewComment> findByReviewId(UUID reviewId) {
        return jpaRepository.findByReviewIdOrderByCreatedAtAsc(reviewId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
