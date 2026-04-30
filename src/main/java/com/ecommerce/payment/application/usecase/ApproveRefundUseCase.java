package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.ApproveRefundCommand;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.mapper.RefundApplicationMapper;
import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.Refund;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * Admin approves a pending refund request.
 * Forwards the request to the payment gateway and updates the refund status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveRefundUseCase {

    private final PaymentRepository paymentRepository;
    private final RefundApplicationMapper mapper;
    private final PaymentGatewayResolver gatewayResolver;
    private final OutboxService outboxService;
    private final TransactionTemplate transactionTemplate;

    public RefundResponse execute(ApproveRefundCommand cmd) {
        // 1. Pre-check (Transactional or non-transactional read)
        Payment payment = paymentRepository.findById(cmd.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Refund refund = payment.getRefunds().stream()
                .filter(r -> r.getId().equals(cmd.refundId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_REFUND_NOT_FOUND));

        // 2. Forward to gateway (OUTSIDE Transaction)
        // If this takes 30s, we are not holding any DB connections!
        String providerRefundId = null;
        if (payment.getProvider().isOnline()) {
            var refundIssuer = gatewayResolver.getRefundIssuer(payment.getProvider());
            providerRefundId = refundIssuer.issueRefund(
                    cmd.refundId(),
                    payment.getProviderReference(),
                    refund.getAmount(),
                    payment.getCurrency()
            );
        } else {
            // COD - generate internal refund ID
            providerRefundId = "COD-REFUND-" + UUID.randomUUID();
        }

        // 3. Persist the result (In a fresh Transaction)
        final String finalProviderRefundId = providerRefundId;
        Payment savedPayment = transactionTemplate.execute(status -> {
            Payment p = paymentRepository.findById(cmd.paymentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

            p.approveRefund(cmd.refundId(), finalProviderRefundId, cmd.approvedAt());
            Payment saved = paymentRepository.save(p);

            // Publish event within the same transaction to ensure consistency (if using transactional event listener)
            // Or right after save. Here we use the aggregate to get the updated refund.
            Refund approvedRefund = saved.getRefunds().stream()
                    .filter(r -> r.getId().equals(cmd.refundId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_REFUND_NOT_FOUND));

            outboxService.saveEvent(approvedRefund.toApprovedEvent(), 
                    saved.getId().toString(), "Payment");
            return saved;
        });

        Refund finalizedRefund = savedPayment.getRefunds().stream()
                .filter(r -> r.getId().equals(cmd.refundId()))
                .findFirst()
                .orElseThrow();

        return mapper.toResponse(finalizedRefund);
    }
}
