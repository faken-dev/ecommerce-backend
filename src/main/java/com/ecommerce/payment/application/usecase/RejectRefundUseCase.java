package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.RejectRefundCommand;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.mapper.RefundApplicationMapper;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.Refund;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin rejects a pending refund request.
 */
@Service
@RequiredArgsConstructor
public class RejectRefundUseCase {

    private final PaymentRepository paymentRepository;
    private final RefundApplicationMapper mapper;
    private final OutboxService outboxService;

    @Transactional
    public RefundResponse execute(RejectRefundCommand cmd) {

        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.rejectRefund(cmd.refundId(), cmd.reason());
        Payment saved = paymentRepository.save(payment);

        Refund rejectedRefund = saved.getRefunds().stream()
                .filter(r -> r.getId().equals(cmd.refundId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_REFUND_NOT_FOUND));

        // Save to outbox for reliable delivery
        outboxService.saveEvent(rejectedRefund.toRejectedEvent(), 
                saved.getId().toString(), "Payment");

        return mapper.toResponse(rejectedRefund);
    }
}
