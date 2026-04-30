package com.ecommerce.audit.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private UUID id;
    private Instant timestamp;
    private UUID userId;
    private String userName;
    private String action;
    private String resourceType;
    private String resourceId;
    private String payload;
    private String ipAddress;
    private String status; // SUCCESS, FAILURE
    private String errorMessage;

    public static AuditLog create(UUID userId, String userName, String action, 
                                 String resourceType, String resourceId, 
                                 String payload, String ipAddress) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .timestamp(Instant.now())
                .userId(userId)
                .userName(userName)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .payload(payload)
                .ipAddress(ipAddress)
                .status("SUCCESS")
                .build();
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILURE";
        this.errorMessage = errorMessage;
    }
}
