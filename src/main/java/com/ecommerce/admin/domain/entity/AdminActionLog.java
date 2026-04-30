package com.ecommerce.admin.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AdminActionLog extends AuditableEntity {
    private UUID adminId;
    private String adminEmail;
    private String action;
    private String resourceType;
    private String resourceId;
    private String details;
    private String ipAddress;
}
