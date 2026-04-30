package com.ecommerce.user.application.usecase;

import java.time.Instant;

import com.ecommerce.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.UserJpaRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCase.class);

    private final UserJpaRepository userJpaRepository;

    @Value("${app.bootstrap.admin-email:}")
    private String bootstrapAdminEmail;

    @Transactional
    public void execute(UUID userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found: " + userId));

        // Prevent deleting the bootstrap admin
        if (user.getEmail().equalsIgnoreCase(bootstrapAdminEmail)) {
            log.warn("Attempted to delete protected bootstrap admin: {}", user.getEmail());
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Cannot delete system bootstrap administrator");
        }

        // Prevent self-deletion
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID currentUserId = null;
        if (principal instanceof UUID) {
            currentUserId = (UUID) principal;
        } else if (principal instanceof String) {
            try {
                currentUserId = UUID.fromString((String) principal);
            } catch (Exception ignored) {}
        }

        if (currentUserId != null && currentUserId.equals(userId)) {
            log.warn("User {} attempted to delete their own account", userId);
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Cannot delete your own account. Use deactivation or contact another administrator.");
        }

        // Soft delete
        log.info("Performing soft delete for user: {} (Deleted by: {})", userId, currentUserId);
        user.setDeletedAt(Instant.now());
        if (currentUserId != null) {
            user.setDeletedBy(currentUserId);
        }
        
        userJpaRepository.save(user);
        log.info("Successfully soft-deleted user: {}", userId);
    }
}
