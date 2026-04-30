package com.ecommerce.payment.application.usecase;

import java.time.Instant;

import com.ecommerce.payment.application.command.CreatePaymentCommand;
import com.ecommerce.payment.application.dto.PaymentInitiatedResponse;
import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.event.PaymentInitiatedEvent;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.outbox.OutboxService;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates a new Payment aggregate and initiates it with the configured gateway.
 *
 * Supports idempotency: if a payment with the same idempotencyKey already exists
 * for the same order, returns the existing record instead of creating a duplicate.
 *
 * <p>Provider-specific redirect URLs are returned in {@link PaymentInitiatedResponse}
 * so the frontend can redirect the user to the payment page (VNPay, MoMo, PayPal).
 */

@Service
@RequiredArgsConstructor
public class CreatePaymentUseCase {
    private static final Logger log = LoggerFactory.getLogger(CreatePaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final PaymentGatewayResolver gatewayResolver;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentInitiatedResponse execute(CreatePaymentCommand cmd) {

        // - Step 1: Persist Intent (Transactional) -------------
        // This reserves the idempotency key and ensures no duplicate active payments for the order.
        final boolean[] isIdempotencyHit = {false};
        final Payment initiatedPayment = transactionTemplate.execute(status -> {
            // Idempotency guard
            if (cmd.idempotencyKey() != null && !cmd.idempotencyKey().isBlank()) {
                var existing = paymentRepository.findByIdempotencyKey(cmd.idempotencyKey());
                if (existing.isPresent()) {
                    isIdempotencyHit[0] = true;
                    return existing.get();
                }
            }

            // Duplicate-order guard
            var orderPayments = paymentRepository.findAllByOrderId(cmd.orderId());
            
            // If any is already PAID, don't allow more
            boolean alreadyPaid = orderPayments.stream()
                    .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);
            if (alreadyPaid) {
                throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PAID,
                        "Don hang nay da duoc thanh toan");
            }

            // If there's an active (non-terminal) payment, we have two choices:
            // 1. Return it (if we can re-generate the URL)
            // 2. Cancel it and allow a new one.
            // Choosing #2 for simplicity and robustness (fresh URL/session).
            orderPayments.stream()
                    .filter(p -> !p.getStatus().isTerminal())
                    .forEach(p -> {
                        log.info("Cancelling stale payment attempt {} for order {}", p.getId(), cmd.orderId());
                        p.setStatus(PaymentStatus.CANCELLED);
                        paymentRepository.save(p);
                    });

            String description = cmd.description() != null
                    ? cmd.description()
                    : "Thanh toan don hang " + cmd.orderId();

            Payment p = Payment.create(
                    cmd.orderId(),
                    cmd.buyerId(),
                    cmd.amount(),
                    cmd.currency(),
                    cmd.provider(),
                    cmd.methodType(),
                    description,
                    cmd.idempotencyKey(),
                    cmd.returnUrl(),
                    cmd.cancelUrl(),
                    cmd.ipAddress(),
                    cmd.userAgent()
            );

            return paymentRepository.save(p);
        });

        // If the payment was an idempotency hit, return immediately as per original logic
        if (isIdempotencyHit[0]) {
            return buildInitiatedResponse(initiatedPayment, null);
        }

        // If the payment is already initiated (not PENDING), also return early
        if (initiatedPayment.getStatus() != PaymentStatus.PENDING) {
            return buildInitiatedResponse(initiatedPayment, null);
        }

        // - Step 2: Gateway Initiation (OUTSIDE Transaction) ---------
        String providerReference = null;
        String paymentPageUrl = null;
        Payment finalPayment = initiatedPayment;

        if (cmd.provider().isOnline()) {
            log.info("Initiating online payment with provider {} for payment {}", cmd.provider(), initiatedPayment.getId());
            var initiator = gatewayResolver.getInitiator(cmd.provider());

            var result = initiator.initiate(
                    initiatedPayment.getId(),
                    cmd.orderId(),
                    cmd.amount(),
                    cmd.currency(),
                    cmd.provider(),
                    cmd.returnUrl(),
                    cmd.cancelUrl(),
                    cmd.ipAddress()
            );

            providerReference = result.providerReference();
            paymentPageUrl = result.paymentPageUrl() != null ? result.paymentPageUrl() : result.clientSecret();

            // - Step 3: Update Status (Transactional) -------------
            final String finalProviderReference = providerReference;
            finalPayment = transactionTemplate.execute(status -> {
                Payment p = paymentRepository.findById(initiatedPayment.getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

                p.initiate(finalProviderReference);
                Payment saved = paymentRepository.save(p);

                // Save to outbox for reliable delivery
                outboxService.saveEvent(new PaymentInitiatedEvent(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getBuyerId(),
                        saved.getAmount(),
                        saved.getCurrency(),
                        saved.getProvider(),
                        saved.getMethodType(),
                        finalProviderReference,
                        saved.getReturnUrl(),
                        saved.getCancelUrl(),
                        Instant.now()
                ), saved.getId().toString(), "Payment");

                return saved;
            });
        } else {
            // For offline (COD), no gateway initiation needed, but mark as PROCESSING to satisfy frontend
            log.info("COD payment created for order {}", cmd.orderId());
            finalPayment = transactionTemplate.execute(status -> {
                Payment p = paymentRepository.findById(initiatedPayment.getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
                
                // For COD, mark as PAID directly since the order is confirmed immediately
                p.setStatus(PaymentStatus.PAID);
                p.setPaidAt(Instant.now());
                p.setProviderReference("COD-" + p.getOrderId().toString().substring(0, 8));
                return paymentRepository.save(p);
            });

            // For COD, publish synchronously so Order is updated in the same request
            eventPublisher.publishEvent(new PaymentInitiatedEvent(
                    finalPayment.getId(),
                    finalPayment.getOrderId(),
                    finalPayment.getBuyerId(),
                    finalPayment.getAmount(),
                    finalPayment.getCurrency(),
                    finalPayment.getProvider(),
                    finalPayment.getMethodType(),
                    finalPayment.getProviderReference(),
                    finalPayment.getReturnUrl(),
                    finalPayment.getCancelUrl(),
                    Instant.now()
            ));

            // For COD, the "payment page" is just the success page
            String successUrl = finalPayment.getReturnUrl();
            if (successUrl != null && !successUrl.isBlank()) {
                successUrl += (successUrl.contains("?") ? "&" : "?") 
                    + "success=true&paymentId=" + finalPayment.getId() 
                    + "&orderId=" + finalPayment.getOrderId();
            }
            paymentPageUrl = successUrl;
        }

        return buildInitiatedResponse(finalPayment, paymentPageUrl);
    }

    private PaymentInitiatedResponse buildInitiatedResponse(Payment payment, String paymentPageUrl) {
        return new PaymentInitiatedResponse(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getProvider().name(),
                payment.getMethodType(),
                payment.getStatus().name(),
                payment.getProviderReference(),
                paymentPageUrl,
                payment.getCancelUrl(),
                "Payment initiated. Redirect to payment page."
        );
    }
}
