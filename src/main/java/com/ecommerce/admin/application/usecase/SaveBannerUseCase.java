package com.ecommerce.admin.application.usecase;

import java.util.List;

import com.ecommerce.admin.application.dto.BannerRequest;
import com.ecommerce.admin.application.dto.BannerResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.domain.entity.Banner;
import com.ecommerce.admin.domain.repository.BannerRepository;
import com.ecommerce.shared.application.service.MediaAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveBannerUseCase {
    private final BannerRepository bannerRepository;
    private final AdminApplicationMapper mapper;
    private final MediaAssetService mediaAssetService;

    @Transactional
    public BannerResponse execute(BannerRequest request) {
        Banner banner;
        if (request.id() != null) {
            banner = bannerRepository.findById(request.id())
                    .orElseGet(() -> Banner.builder().id(request.id()).build());
        } else {
            banner = Banner.builder().build(); // AuditableEntity handles UUID by default
        }

        banner.updateInfo(
            request.title(),
            request.imageUrl(),
            request.linkUrl(),
            request.status(),
            request.priority()
        );

        Banner saved = bannerRepository.save(banner);
        
        // Mark image as ACTIVE
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            mediaAssetService.markAsActive(List.of(request.imageUrl()));
        }

        return mapper.toBannerResponse(saved);
    }
}
