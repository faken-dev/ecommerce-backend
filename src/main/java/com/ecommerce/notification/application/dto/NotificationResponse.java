package com.ecommerce.notification.application.dto;

import com.ecommerce.notification.domain.entity.InAppNotification.NotificationStatus;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String content,
        NotificationType type,
        NotificationStatus status,
        String actionUrl,
        Instant createdAt
) {}
