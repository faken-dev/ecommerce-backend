package com.ecommerce.admin.application.mapper;

import com.ecommerce.admin.application.dto.AdminActionLogResponse;
import com.ecommerce.admin.application.dto.BannerResponse;
import com.ecommerce.admin.application.dto.StaticPageResponse;
import com.ecommerce.admin.domain.entity.Banner;
import com.ecommerce.admin.domain.entity.StaticPage;
import com.ecommerce.admin.infrastructure.persistence.entity.AdminActionLogJpaEntity;
import com.ecommerce.admin.infrastructure.persistence.entity.BannerJpaEntity;
import com.ecommerce.admin.infrastructure.persistence.entity.StaticPageJpaEntity;
import com.ecommerce.shared.application.mapper.DateMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface AdminApplicationMapper {
    BannerResponse toBannerResponse(Banner banner);
    BannerResponse toBannerResponse(BannerJpaEntity entity);
    List<BannerResponse> toBannerResponseList(List<BannerJpaEntity> entities);

    @Mapping(target = "targetType", source = "resourceType")
    @Mapping(target = "targetId", source = "resourceId")
    AdminActionLogResponse toAdminActionLogResponse(AdminActionLogJpaEntity entity);
    List<AdminActionLogResponse> toAdminActionLogResponseList(List<AdminActionLogJpaEntity> entities);

    @Mapping(target = "isActive", source = "active")
    StaticPageResponse toStaticPageResponse(StaticPage page);

    @Mapping(target = "isActive", source = "active")
    StaticPageResponse toStaticPageResponse(StaticPageJpaEntity entity);

    List<StaticPageResponse> toStaticPageResponseList(List<StaticPageJpaEntity> entities);
}
