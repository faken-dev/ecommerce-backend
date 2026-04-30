package com.ecommerce.notification.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.ecommerce.notification.domain.entity.InAppNotification;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.notification.infrastructure.service.NotificationPushService;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastNotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(BroadcastNotificationUseCase.class);

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final TransactionTemplate transactionTemplate;

    private static final int BATCH_SIZE = 100;

    @Async
    public void execute(String title, String content, NotificationType type, String actionUrl, String role) {
        log.info("Starting ASYNC broadcast notification: title='{}', role='{}'", title, role);
        
        int pageNumber = 0;
        long totalProcessed = 0;
        boolean hasNext;

        do {
            final int currentPage = pageNumber;
            
            // We use a transaction for each batch to keep the session open for lazy loading 
            // and to perform the batch save in one transaction.
            hasNext = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                PageRequest pageRequest = PageRequest.of(currentPage, BATCH_SIZE);
                Page<User> userPage;
                
                if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("ALL")) {
                    userPage = userRepository.searchUsers(role, null, pageRequest);
                } else {
                    userPage = userRepository.findAll(pageRequest);
                }

                List<InAppNotification> batch = new ArrayList<>();
                userPage.getContent().forEach(user -> {
                    // This is where LazyInitializationException would happen if not in transaction
                    InAppNotification notification = InAppNotification.create(user.getId(), title, content, type, actionUrl);
                    batch.add(notification);
                });

                // Batch save to database
                if (!batch.isEmpty()) {
                    notificationRepository.saveAll(batch);
                    // Push real-time (can be done inside or outside transaction, inside is safer for data consistency)
                    batch.forEach(pushService::push);
                }

                return userPage.hasNext();
            }));

            totalProcessed += BATCH_SIZE; // Approximation or we could count accurately
            pageNumber++;
            
        } while (hasNext);

        log.info("Async broadcast finished. Approximated total users notified: {}", totalProcessed);
    }
}
