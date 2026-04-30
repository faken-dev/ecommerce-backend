package com.ecommerce.notification.presentation.controller;

import com.ecommerce.notification.application.usecase.BroadcastNotificationUseCase;
import com.ecommerce.notification.application.usecase.SendNotificationUseCase;
import com.ecommerce.notification.domain.entity.InAppNotification.NotificationType;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.ecommerce.shared.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notification", description = "Administrator notification management")
public class AdminNotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final BroadcastNotificationUseCase broadcastNotificationUseCase;

    public record SendNotificationRequest(
            UUID userId,
            String title,
            String content,
            NotificationType type,
            String actionUrl,
            boolean broadcast,
            String targetRole
    ) {}

    @Operation(summary = "Send notification to user or broadcast")
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('notification:manage')")
    public ResponseEntity<ApiResponse<Void>> sendNotification(@RequestBody SendNotificationRequest request) {
        if (request.broadcast()) {
            broadcastNotificationUseCase.execute(request.title(), request.content(), request.type(), request.actionUrl(), request.targetRole());
        } else if (request.userId() != null) {
            sendNotificationUseCase.execute(request.userId(), request.title(), request.content(), request.type(), request.actionUrl());
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_FAILED, "User ID or broadcast must be specified"));
        }
        return ResponseEntity.ok(ApiResponse.ok((Void) null));
    }
}


