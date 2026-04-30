package com.ecommerce.notification.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;

public class InAppNotification extends AuditableEntity {

    private UUID userId;
    private String title;
    private String content;
    private NotificationType type;
    private NotificationStatus status;
    private String actionUrl;
    private String metadata; // JSON metadata

    public InAppNotification() {}

    public enum NotificationType {
        ORDER, PROMO, SYSTEM, SOCIAL
    }

    public enum NotificationStatus {
        UNREAD, READ
    }

    public static InAppNotification create(UUID userId, String title, String content, NotificationType type, String actionUrl) {
        Instant now = Instant.now();
        InAppNotification notification = new InAppNotification();
        notification.setId(UuidCreator.getTimeOrderedEpoch());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setActionUrl(actionUrl);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        return notification;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.touchUpdate();
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    // Builder pattern fallback
    public static InAppNotificationBuilder builder() {
        return new InAppNotificationBuilder();
    }

    public static class InAppNotificationBuilder {
        private final InAppNotification instance = new InAppNotification();

        public InAppNotificationBuilder id(UUID id) { instance.setId(id); return this; }
        public InAppNotificationBuilder userId(UUID userId) { instance.setUserId(userId); return this; }
        public InAppNotificationBuilder title(String title) { instance.setTitle(title); return this; }
        public InAppNotificationBuilder content(String content) { instance.setContent(content); return this; }
        public InAppNotificationBuilder type(NotificationType type) { instance.setType(type); return this; }
        public InAppNotificationBuilder status(NotificationStatus status) { instance.setStatus(status); return this; }
        public InAppNotificationBuilder actionUrl(String actionUrl) { instance.setActionUrl(actionUrl); return this; }
        public InAppNotificationBuilder metadata(String metadata) { instance.setMetadata(metadata); return this; }
        public InAppNotificationBuilder createdAt(Instant createdAt) { instance.setCreatedAt(createdAt); return this; }
        public InAppNotificationBuilder updatedAt(Instant updatedAt) { instance.setUpdatedAt(updatedAt); return this; }

        public InAppNotification build() {
            return instance;
        }
    }
}
