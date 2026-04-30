package com.ecommerce.audit.infrastructure.persistence.impl;

import com.ecommerce.audit.domain.entity.AuditLog;
import com.ecommerce.audit.domain.repository.AuditLogRepository;
import com.ecommerce.audit.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.ecommerce.audit.infrastructure.persistence.mapper.AuditLogMapper;
import com.ecommerce.audit.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogMapper mapper;

    @Override
    public void save(AuditLog auditLog) {
        AuditLogJpaEntity jpa = mapper.toJpa(auditLog);
        jpaRepository.save(jpa);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }
}
