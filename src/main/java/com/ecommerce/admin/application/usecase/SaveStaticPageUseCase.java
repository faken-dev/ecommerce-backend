package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.application.dto.StaticPageRequest;
import com.ecommerce.admin.application.dto.StaticPageResponse;
import com.ecommerce.admin.application.mapper.AdminApplicationMapper;
import com.ecommerce.admin.infrastructure.persistence.entity.StaticPageJpaEntity;
import com.ecommerce.admin.infrastructure.persistence.repository.StaticPageJpaRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SaveStaticPageUseCase {
    private final StaticPageJpaRepository staticPageJpaRepository;
    private final AdminApplicationMapper mapper;

    @Transactional
    public StaticPageResponse execute(StaticPageRequest request) {
        StaticPageJpaEntity entity;
        if (request.id() != null) {
            entity = staticPageJpaRepository.findById(request.id())
                    .orElseGet(() -> {
                        StaticPageJpaEntity newEntity = new StaticPageJpaEntity();
                        newEntity.setId(request.id());
                        newEntity.setCreatedAt(Instant.now());
                        return newEntity;
                    });
        } else {
            entity = new StaticPageJpaEntity();
            entity.setId(UuidCreator.getTimeOrderedEpoch());
            entity.setCreatedAt(Instant.now());
        }

        entity.setTitle(request.title());
        entity.setSlug(request.slug());
        entity.setContent(request.content());
        entity.setActive(request.isActive() != null ? request.isActive() : true);

        return mapper.toStaticPageResponse(staticPageJpaRepository.save(entity));
    }
}
