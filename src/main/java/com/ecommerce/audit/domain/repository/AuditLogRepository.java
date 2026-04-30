package com.ecommerce.audit.domain.repository;

import com.ecommerce.audit.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository {
    void save(AuditLog auditLog);
    Optional<AuditLog> findById(UUID id);
    Page<AuditLog> findAll(Pageable pageable);
}
