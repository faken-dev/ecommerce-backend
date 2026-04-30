package com.ecommerce.admin.infrastructure.persistence.mapper;

import com.ecommerce.admin.domain.entity.Banner;
import com.ecommerce.admin.infrastructure.persistence.entity.BannerJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BannerPersistenceMapper {

    public Banner toDomain(BannerJpaEntity entity) {
        if (entity == null) return null;
        return Banner.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public BannerJpaEntity toJpa(Banner domain) {
        if (domain == null) return null;
        return BannerJpaEntity.builder()
                .id(domain.getId())
                .imageUrl(domain.getImageUrl())
                .linkUrl(domain.getLinkUrl())
                .title(domain.getTitle())
                .status(domain.getStatus())
                .priority(domain.getPriority())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }
}
