package com.ecommerce.audit.presentation.controller;

import com.ecommerce.audit.domain.entity.AuditLog;
import com.ecommerce.audit.domain.repository.AuditLogRepository;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Admin audit log management")
@PreAuthorize("hasAuthority('audit:read')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @Operation(summary = "Get all audit logs (Admin only)")
    @GetMapping
    public ApiResponse<Iterable<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.findAll(pageRequest);
        
        return ApiResponse.ok(logs);
    }
}


