package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.mapper.PaymentApplicationMapper;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Retrieves a single payment by ID, enforcing buyer ownership.
 */
@Service
@RequiredArgsConstructor
public class GetPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentApplicationMapper mapper;

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID paymentId, UUID buyerId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.isOwnedBy(buyerId)) {
            throw new BusinessException(ErrorCode.PAYMENT_FORBIDDEN);
        }

        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse executeForAdmin(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return mapper.toResponse(payment);
    }
}
