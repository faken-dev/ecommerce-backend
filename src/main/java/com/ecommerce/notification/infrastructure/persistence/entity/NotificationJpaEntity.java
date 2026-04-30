package com.ecommerce.notification.infrastructure.persistence.entity;

import com.ecommerce.notification.domain.entity.InAppNotification.NotificationStatus;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.shared.infrastructure.persistence.VersionedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "user_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationJpaEntity extends VersionedJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
