package com.ecommerce.notification.application.usecase;

import com.ecommerce.notification.domain.entity.InAppNotification;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.notification.infrastructure.service.NotificationPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendNotificationUseCase {

    private final NotificationRepository repository;
    private final NotificationPushService pushService;

    @Async
    @Transactional
    public void execute(UUID userId, String title, String content, NotificationType type, String actionUrl) {
        InAppNotification notification = InAppNotification.create(userId, title, content, type, actionUrl);
        
        // 1. Save to database for history
        repository.save(notification);
        
        // 2. Push real-time via WebSocket
        pushService.push(notification);
    }
}
