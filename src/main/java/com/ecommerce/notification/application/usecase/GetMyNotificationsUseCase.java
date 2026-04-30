package com.ecommerce.notification.application.usecase;

import com.ecommerce.notification.application.dto.NotificationResponse;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyNotificationsUseCase {

    private final NotificationRepository repository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> execute(UUID userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable)
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getContent(),
                        n.getType(),
                        n.getStatus(),
                        n.getActionUrl(),
                        n.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return repository.countUnread(userId);
    }
}
