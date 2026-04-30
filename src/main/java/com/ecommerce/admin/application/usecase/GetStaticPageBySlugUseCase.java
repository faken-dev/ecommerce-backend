package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.application.dto.StaticPageResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.infrastructure.persistence.repository.StaticPageJpaRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetStaticPageBySlugUseCase {
    private final StaticPageJpaRepository staticPageJpaRepository;
    private final AdminApplicationMapper mapper;

    @Transactional(readOnly = true)
    public StaticPageResponse execute(String slug) {
        return staticPageJpaRepository.findBySlug(slug)
                .map(mapper::toStaticPageResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Page not found"));
    }
}
