package com.ecommerce.notification.application.port;

public interface NotificationSender {
    void send(NotificationMessage message);
    boolean supports(NotificationChannel channel);
}
