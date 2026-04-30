package com.ecommerce.audit.infrastructure.persistence.mapper;

import com.ecommerce.audit.domain.entity.AuditLog;
import com.ecommerce.audit.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogJpaEntity toJpa(AuditLog domain);
    AuditLog toDomain(AuditLogJpaEntity jpa);
}
