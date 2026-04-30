package com.ecommerce.feedback.application.usecase;

import com.ecommerce.feedback.domain.entity.ReviewComment;
import com.ecommerce.feedback.domain.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCommentUseCase {

    private final ReviewCommentRepository repository;

    @Transactional
    public void execute(UUID userId, UUID reviewId, UUID parentCommentId, String content) {
        ReviewComment comment = ReviewComment.create(reviewId, userId, parentCommentId, content).build();
        repository.save(comment);
    }
}
