package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.RequestRefundCommand;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.mapper.RefundApplicationMapper;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Buyer or admin requests a refund for a paid order.
 * The refund enters PENDING status and awaits admin approval.
 */
@Service
@RequiredArgsConstructor
public class RequestRefundUseCase {

    private final PaymentRepository paymentRepository;
    private final RefundApplicationMapper mapper;
    private final OutboxService outboxService;

    @Transactional
    public RefundResponse execute(RequestRefundCommand cmd) {

        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Payment.RefundRequestResult result = payment.requestRefund(
                cmd.orderId(),
                cmd.requestedBy(),
                cmd.amount(),
                cmd.reason(),
                cmd.ipAddress()
        );

        paymentRepository.save(payment);
        outboxService.saveEvent(result.event(), payment.getId().toString(), "Payment");

        return mapper.toResponse(result.refund());
    }
}
