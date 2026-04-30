package com.ecommerce.notification.application.usecase;

import com.ecommerce.notification.domain.entity.InAppNotification;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkNotificationReadUseCase {

    private final NotificationRepository repository;

    @Transactional
    public void execute(UUID notificationId, UUID userId) {
        InAppNotification notification = repository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "You cannot mark this notification as read");
        }

        notification.markAsRead();
        repository.save(notification);
    }
}
