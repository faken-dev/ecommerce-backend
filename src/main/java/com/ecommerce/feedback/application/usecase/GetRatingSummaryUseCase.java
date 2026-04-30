package com.ecommerce.feedback.application.usecase;

import com.ecommerce.feedback.domain.repository.ReviewRepository;
import com.ecommerce.feedback.presentation.dto.response.RatingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRatingSummaryUseCase {
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public RatingSummaryResponse execute(UUID productId) {
        return reviewRepository.getRatingSummary(productId);
    }
}
