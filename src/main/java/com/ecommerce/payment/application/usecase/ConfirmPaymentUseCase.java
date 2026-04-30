package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.ConfirmPaymentCommand;
import com.ecommerce.payment.application.dto.PaymentConfirmedResponse;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirms a payment after receiving the webhook from the payment gateway.
 *
 * <p>Responsibility: update the Payment aggregate only.
 * The corresponding Order status update is handled by {@code PaymentEventHandler}
 * reacting to the {@code PaymentConfirmedEvent} published here.
 */
@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentConfirmedResponse execute(ConfirmPaymentCommand cmd) {

        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.confirm(cmd.providerReference(), cmd.paidAt());
        Payment saved = paymentRepository.save(payment);

        // Save to outbox for reliable delivery
        var event = saved.toConfirmedEvent();
        outboxService.saveEvent(event, saved.getId().toString(), "Payment");
        
        // Also publish synchronously for immediate Order update
        eventPublisher.publishEvent(event);

        return new PaymentConfirmedResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getProvider(),
                saved.getProviderReference(),
                saved.getPaidAt(),
                "Payment confirmed successfully"
        );
    }
}
