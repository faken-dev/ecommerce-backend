package com.ecommerce.notification.domain.repository;

import com.ecommerce.notification.domain.entity.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    void save(InAppNotification notification);
    void saveAll(List<InAppNotification> notifications);
    Optional<InAppNotification> findById(UUID id);
    Page<InAppNotification> findByUserId(UUID userId, Pageable pageable);
    long countUnread(UUID userId);
}
