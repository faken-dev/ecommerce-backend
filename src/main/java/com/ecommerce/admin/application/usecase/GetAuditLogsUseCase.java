package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.application.dto.AdminActionLogResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.infrastructure.persistence.repository.AdminActionLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAuditLogsUseCase {
    private final AdminActionLogJpaRepository adminActionLogJpaRepository;
    private final AdminApplicationMapper mapper;

    @Transactional(readOnly = true)
    public List<AdminActionLogResponse> execute() {
        return mapper.toAdminActionLogResponseList(
                adminActionLogJpaRepository.findTop10ByOrderByCreatedAtDesc());
    }
}
