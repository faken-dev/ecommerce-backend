package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.CancelPaymentCommand;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cancels a payment at buyer's request before it is confirmed.
 */
@Service
@RequiredArgsConstructor
public class CancelPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID execute(CancelPaymentCommand cmd) {

        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.cancel(cmd.cancelledBy());
        Payment saved = paymentRepository.save(payment);

        // Save to outbox for reliable delivery
        var event = saved.toCancelledEvent(cmd.cancelledBy());
        outboxService.saveEvent(event, saved.getId().toString(), "Payment");
        
        // Also publish synchronously
        eventPublisher.publishEvent(event);

        return saved.getId();
    }
}
