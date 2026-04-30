package com.ecommerce.notification.application.listener;

import com.ecommerce.notification.application.usecase.SendNotificationUseCase;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final SendNotificationUseCase sendNotificationUseCase;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent for notification: {}", event.orderId());

        String shortOrderId = event.orderId().toString().substring(0, 8);

        // Notify Buyer
        sendNotificationUseCase.execute(
                event.buyerId(),
                "Đặt hàng thành công",
                "Đơn hàng #" + shortOrderId + " đã được tạo thành công.",
                NotificationType.ORDER,
                "/buyer/orders/" + event.orderId()
        );

        // Notify Seller
        sendNotificationUseCase.execute(
                event.sellerId(),
                "Có đơn hàng mới",
                "Bạn vừa nhận được một đơn hàng mới #" + shortOrderId,
                NotificationType.ORDER,
                "/seller/orders/" + event.orderId()
        );
    }
}