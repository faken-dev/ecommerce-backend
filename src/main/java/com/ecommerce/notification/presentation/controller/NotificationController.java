package com.ecommerce.notification.presentation.controller;


import com.ecommerce.notification.application.dto.NotificationResponse;
import com.ecommerce.notification.application.usecase.GetMyNotificationsUseCase;
import com.ecommerce.notification.application.usecase.MarkNotificationReadUseCase;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "In-app and system notification management")
public class NotificationController {

    private final GetMyNotificationsUseCase getMyNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    @Operation(summary = "Get current user notifications")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Iterable<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(getMyNotificationsUseCase.execute(userId, pageable)));
    }

    @Operation(summary = "Get unread count")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(getMyNotificationsUseCase.countUnread(userId)));
    }

    @Operation(summary = "Mark notification as read")
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        markNotificationReadUseCase.execute(id, userId);
        return ResponseEntity.ok(ApiResponse.ok((Void) null));
    }
}


