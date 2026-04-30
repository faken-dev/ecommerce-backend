package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.application.dto.StaticPageResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.infrastructure.persistence.repository.StaticPageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllStaticPagesUseCase {
    private final StaticPageJpaRepository staticPageJpaRepository;
    private final AdminApplicationMapper mapper;

    @Transactional(readOnly = true)
    public List<StaticPageResponse> execute() {
        return mapper.toStaticPageResponseList(staticPageJpaRepository.findAll());
    }
}
