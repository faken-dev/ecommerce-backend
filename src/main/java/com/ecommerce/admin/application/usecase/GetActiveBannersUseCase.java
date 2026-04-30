package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.application.dto.BannerResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.infrastructure.persistence.repository.BannerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActiveBannersUseCase {
    private final BannerJpaRepository bannerJpaRepository;
    private final AdminApplicationMapper mapper;

    @Transactional(readOnly = true)
    public List<BannerResponse> execute() {
        return mapper.toBannerResponseList(
                bannerJpaRepository.findByStatusOrderByPriorityDesc("ACTIVE"));
    }
}
