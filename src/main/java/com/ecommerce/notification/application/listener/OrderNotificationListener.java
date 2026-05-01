package com.ecommerce.notification.application.listener;

import com.ecommerce.notification.application.usecase.SendNotificationUseCase;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderConfirmedEvent;
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
        boolean isCod = "COD".equalsIgnoreCase(event.paymentMethod());
        
        String buyerTitle = isCod ? "Đặt hàng thành công" : "Đã nhận đơn hàng";
        String buyerContent = isCod 
                ? "Đơn hàng #" + shortOrderId + " đã được tạo thành công."
                : "Chúng tôi đã nhận được đơn hàng #" + shortOrderId + " của bạn và đang chờ thanh toán.";

        // Notify Buyer
        sendNotificationUseCase.execute(
                event.buyerId(),
                buyerTitle,
                buyerContent,
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

    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Handling OrderConfirmedEvent for notification: {}", event.orderId());

        String shortOrderId = event.orderId().toString().substring(0, 8);

        // Notify Buyer of Payment Success
        sendNotificationUseCase.execute(
                event.buyerId(),
                "Thanh toán thành công",
                "Đơn hàng #" + shortOrderId + " của bạn đã được thanh toán thành công và đang được xử lý.",
                NotificationType.ORDER,
                "/buyer/orders/" + event.orderId()
        );

        // Notify Seller of Payment
        sendNotificationUseCase.execute(
                event.sellerId(),
                "Đơn hàng đã thanh toán",
                "Đơn hàng #" + shortOrderId + " đã được khách hàng thanh toán thành công.",
                NotificationType.ORDER,
                "/seller/orders/" + event.orderId()
        );
    }
}