package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.mapper.PaymentApplicationMapper;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lists payments with optional filters.
 */
@Service
@RequiredArgsConstructor
public class ListPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentApplicationMapper mapper;

    @Transactional(readOnly = true)
    public Page<PaymentResponse> executeByBuyer(UUID buyerId, PaymentStatus status, Pageable pageable) {
        Page<Payment> page = (status != null)
                ? paymentRepository.findByBuyerIdAndStatus(buyerId, status, pageable)
                : paymentRepository.findByBuyerId(buyerId, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> executeForAdmin(PaymentStatus status, Pageable pageable) {
        Page<Payment> page = (status != null)
                ? paymentRepository.findByStatus(status, pageable)
                : paymentRepository.findAll(pageable);
        return page.map(mapper::toResponse);
    }
}
