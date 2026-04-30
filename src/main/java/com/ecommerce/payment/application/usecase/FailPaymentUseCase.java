package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.FailPaymentCommand;
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
 * Records a failed payment from the gateway webhook.
 * Supports idempotent replay - duplicate webhook calls return the existing ID
 * without re-publishing events.
 *
 * Publishes PaymentFailedEvent AFTER the transaction commits
 * (via PaymentEventHandler's @TransactionalEventListener),
 * which triggers order cancellation in the Order module.
 */
@Service
@RequiredArgsConstructor
public class FailPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID execute(FailPaymentCommand cmd) {

        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // - Idempotency: skip if already failed -------------
        if (payment.getStatus().isFailed()) {
            return payment.getId();
        }

        payment.fail(cmd.failureCode(), cmd.failureReason());
        Payment saved = paymentRepository.save(payment);

        // Save to outbox for reliable delivery
        var event = saved.toFailedEvent();
        outboxService.saveEvent(event, saved.getId().toString(), "Payment");
        
        // Also publish synchronously
        eventPublisher.publishEvent(event);

        return saved.getId();
    }
}
