package com.ecommerce.voucher.application.listener;

import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderCreatedEvent.OrderItemData;
import com.ecommerce.voucher.application.command.ApplyVoucherCommand;
import com.ecommerce.voucher.application.usecase.ApplyVoucherUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Listens for OrderCreatedEvent from the Order module.
 * If a voucher code is present, it triggers ApplyVoucherUseCase to record usage.
 */
@Component
@RequiredArgsConstructor

public class OrderCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final ApplyVoucherUseCase applyVoucherUseCase;

    @EventListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event.voucherCode() == null || event.voucherCode().isBlank()) {
            return;
        }

        log.info("Order created with voucher code: {}, applying usage record for order: {}", 
                event.voucherCode(), event.orderId());

        try {
            Set<UUID> productIds = event.items().stream()
                    .map(OrderItemData::productId)
                    .collect(Collectors.toSet());

            ApplyVoucherCommand cmd = new ApplyVoucherCommand(
                    event.voucherCode(),
                    event.buyerId(),
                    event.orderId(),
                    event.totalAmount().add(event.discountAmount()), // Approximate subtotal
                    null, // shippingFee
                    productIds,
                    event.categoryIds()
            );

            applyVoucherUseCase.execute(cmd);
            log.info("Successfully recorded voucher usage for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to apply voucher usage for order: {}. Reason: {}", 
                    event.orderId(), e.getMessage());
            // We don't rethrow because we don't want to fail the order creation 
            // if just the voucher recording fails (business decision, could be changed).
        }
    }
}
