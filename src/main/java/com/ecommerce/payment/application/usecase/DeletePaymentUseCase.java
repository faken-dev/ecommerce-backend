package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletePaymentUseCase {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void execute(UUID paymentId) {
        if (!paymentRepository.findById(paymentId).isPresent()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found");
        }
        paymentRepository.deleteById(paymentId);
    }
}
