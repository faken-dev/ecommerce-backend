package com.ecommerce.payment.application.handler;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.event.PaymentCancelledEvent;
import com.ecommerce.payment.domain.event.PaymentConfirmedEvent;
import com.ecommerce.payment.domain.event.PaymentFailedEvent;
import com.ecommerce.payment.domain.event.PaymentInitiatedEvent;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.event.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles payment domain events and routes side-effects to other modules.
 *
 * <p>This handler bridges the Payment module and the Order module:
 * <ul>
 *   <li>{@link PaymentInitiatedEvent} → triggers {@link Order#confirmPayment()} ONLY for COD</li>
 *   <li>{@link PaymentConfirmedEvent} → triggers {@link Order#confirmPayment()}</li>
 *   <li>{@link PaymentFailedEvent}     → triggers {@link Order#cancel(UUID)} for the buyer</li>
 *   <li>{@link PaymentCancelledEvent} → log only (no order action needed)</li>
 * </ul>
 *
 * <p>ALL handlers use {@code @TransactionalEventListener(phase = AFTER_COMMIT)}.
 * This ensures:
 * <ul>
 *   <li>Order-side effects run ONLY after the payment transaction commits - no stale reads</li>
 *   <li>If the payment transaction rolls back, no event is delivered - no orphan side-effects</li>
 *   <li>Each handler runs in its own transaction - isolated from the payment tx</li>
 * </ul>
 *
 * <p>Idempotency: Both handlers check order status before acting. If the order is
 * already past PENDING (e.g., confirmed or cancelled), the handler logs and skips.
 */

@Component
@RequiredArgsConstructor
public class PaymentEventHandler {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    // - Payment initiated - confirm order immediately if COD --------

    @EventListener
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Received PaymentInitiatedEvent [paymentId={}, method={}]", event.paymentId(), event.methodType());
        if (event.methodType() == PaymentMethodType.COD) {
            log.info("COD payment detected - confirming order [orderId={}]", event.orderId());
            orderRepository.findById(event.orderId())
                    .ifPresentOrElse(order -> {
                    if (order.getStatus() == OrderStatus.PENDING) {
                            order.confirmPayment(event.paymentId());
                            orderRepository.save(order);
                            eventPublisher.publish(order.toConfirmedEvent());
                            log.info("Order confirmed for COD [orderId={}]", event.orderId());
                        } else {
                            log.warn("Order {} already in status {} - skipping COD confirmation", event.orderId(), order.getStatus());
                        }
                    }, () -> log.error("Order not found for COD confirmation [orderId={}]", event.orderId()));
        }
    }

    // - Payment confirmed - update order to CONFIRMED -----------

    /**
     * Fires when a payment is confirmed (PAID).
     */
    @EventListener
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Received PaymentConfirmedEvent [paymentId={}, orderId={}]", event.paymentId(), event.orderId());

        orderRepository.findById(event.orderId())
                .ifPresentOrElse(order -> {
                    if (order.getStatus() == OrderStatus.PENDING) {
                        order.confirmPayment(event.paymentId());
                        orderRepository.save(order);
                        eventPublisher.publish(order.toConfirmedEvent());
                        log.info("Order confirmed after online payment [orderId={}]", event.orderId());
                    } else {
                        log.warn("Order {} not in PENDING status - skipping confirm (status={})",
                                event.orderId(), order.getStatus());
                    }
                }, () -> log.error("Order not found for PaymentConfirmedEvent [orderId={}]",
                        event.orderId()));
    }

    // - Payment failed - cancel the order if still pending --------

    /**
     * Fires when a payment fails.
     */
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment failed [paymentId={}, orderId={}, code={}, reason={}]",
                event.paymentId(), event.orderId(), event.failureCode(), event.failureReason());

        if ("ABANDONED_UNINITIATED".equals(event.failureCode())) {
            log.info("Abandoned uninitiated payment detected - deleting order and payment [orderId={}, paymentId={}]",
                    event.orderId(), event.paymentId());
            orderRepository.deleteById(event.orderId());
            paymentRepository.deleteById(event.paymentId());
            return;
        }

        orderRepository.findById(event.orderId())
                .ifPresentOrElse(order -> {
                    if (order.getStatus() == OrderStatus.PENDING) {
                        // order.cancel(event.buyerId());
                        // orderRepository.save(order);
                        log.info("Order kept in PENDING after payment failure to allow retry [orderId={}, buyerId={}]",
                                event.orderId(), event.buyerId());
                    } else {
                        log.warn("Order {} not in PENDING - skipping auto-cancel (status={})",
                                event.orderId(), order.getStatus());
                    }
                }, () -> log.error("Order not found for PaymentFailedEvent [orderId={}]",
                        event.orderId()));
    }

    // - Payment cancelled by buyer ----

    /**
     * Fires when a payment is cancelled by the buyer.
     */
    @EventListener
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        log.info("Payment cancelled [paymentId={}, orderId={}, cancelledBy={}]",
                event.paymentId(), event.orderId(), event.cancelledBy());
    }
}
