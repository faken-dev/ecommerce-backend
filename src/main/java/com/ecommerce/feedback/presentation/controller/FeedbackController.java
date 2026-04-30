package com.ecommerce.feedback.presentation.controller;


import com.ecommerce.feedback.application.dto.ReviewResponse;
import com.ecommerce.feedback.application.usecase.GetProductReviewsUseCase;
import com.ecommerce.feedback.application.usecase.GetRatingSummaryUseCase;
import com.ecommerce.feedback.application.usecase.PostCommentUseCase;
import com.ecommerce.feedback.application.usecase.PostReviewUseCase;
import com.ecommerce.feedback.presentation.dto.response.RatingSummaryResponse;
import com.ecommerce.feedback.presentation.dto.request.PostCommentRequest;
import com.ecommerce.feedback.presentation.dto.request.PostReviewRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Product reviews and comments management")
public class FeedbackController {

    private final PostReviewUseCase postReviewUseCase;
    private final PostCommentUseCase postCommentUseCase;
    private final GetProductReviewsUseCase getProductReviewsUseCase;
    private final GetRatingSummaryUseCase getRatingSummaryUseCase;

    @Operation(summary = "Submit a product review")
    @PostMapping("/reviews")
    @PreAuthorize("hasAuthority('order:read')") // Assuming verified buyers have this
    public ResponseEntity<ApiResponse<Void>> postReview(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid PostReviewRequest request) {
        postReviewUseCase.execute(
                userId,
                request.productId(),
                request.orderId(),
                request.rating(),
                request.content(),
                request.images()
        );
        return ResponseEntity.ok(ApiResponse.ok((Void) null));
    }

    @Operation(summary = "Get reviews for a product")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Iterable<ReviewResponse>>> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(getProductReviewsUseCase.execute(productId, pageable)));
    }

    @Operation(summary = "Get rating summary for a product")
    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getProductRatingSummary(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(getRatingSummaryUseCase.execute(productId)));
    }

    @Operation(summary = "Reply to a review")
    @PostMapping("/reviews/{reviewId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> postComment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID reviewId,
            @RequestBody @Valid PostCommentRequest request) {
        postCommentUseCase.execute(
                userId,
                reviewId,
                request.parentCommentId(),
                request.content()
        );
        return ResponseEntity.ok(ApiResponse.ok((Void) null));
    }
}


