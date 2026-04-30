package com.ecommerce.notification.infrastructure.persistence.impl;

import com.ecommerce.notification.domain.entity.InAppNotification;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationStatus;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import com.ecommerce.notification.infrastructure.persistence.mapper.NotificationDomainMapper;
import com.ecommerce.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationDomainMapper mapper;

    @Override
    public void save(InAppNotification notification) {
        NotificationJpaEntity entity = jpaRepository.findById(notification.getId())
                .orElseGet(() -> {
                    NotificationJpaEntity newEntity = new NotificationJpaEntity();
                    newEntity.setId(notification.getId());
                    return newEntity;
                });
        
        mapper.updateJpaEntity(notification, entity);
        jpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<InAppNotification> notifications) {
        List<NotificationJpaEntity> entities = notifications.stream()
            .map(n -> {
                NotificationJpaEntity entity = new NotificationJpaEntity();
                entity.setId(n.getId());
                mapper.updateJpaEntity(n, entity);
                return entity;
            })
            .toList();
        jpaRepository.saveAll(entities);
    }

    @Override
    public Optional<InAppNotification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<InAppNotification> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public long countUnread(UUID userId) {
        return jpaRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }
}
