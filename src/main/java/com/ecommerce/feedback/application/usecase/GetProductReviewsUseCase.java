package com.ecommerce.feedback.application.usecase;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.feedback.application.dto.CommentResponse;
import com.ecommerce.feedback.application.dto.ReviewResponse;
import com.ecommerce.feedback.domain.repository.ReviewCommentRepository;
import com.ecommerce.feedback.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductReviewsUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> execute(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(review -> {
            List<CommentResponse> replies = commentRepository.findByReviewId(review.getId()).stream()
                    .map(c -> new CommentResponse(
                            c.getId(),
                            c.getUserId(),
                            getUserName(c.getUserId()),
                            c.getContent(),
                            c.getCreatedAt()
                    )).collect(Collectors.toList());

            return new ReviewResponse(
                    review.getId(),
                    review.getUserId(),
                    getUserName(review.getUserId()),
                    review.getRating(),
                    review.getContent(),
                    review.getImages(),
                    review.getCreatedAt(),
                    replies
            );
        });
    }

    private String getUserName(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse("Anonymous");
    }
}
