package com.ecommerce.notification.infrastructure.service;

import com.ecommerce.notification.domain.entity.InAppNotification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class NotificationPushService {
    private static final Logger log = LoggerFactory.getLogger(NotificationPushService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public void push(InAppNotification notification) {
        String destination = "/queue/notifications";
        log.info("Pushing real-time notification to user {}: {}", notification.getUserId(), notification.getTitle());
        
        // Send to specific user using Spring Simp's convertAndSendToUser
        // This will send to /user/{userId}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                notification.getUserId().toString(),
                destination,
                notification
        );
    }
}
