package com.ecommerce.admin.infrastructure.persistence.repository;

import com.ecommerce.admin.domain.entity.Banner;
import com.ecommerce.admin.domain.repository.BannerRepository;
import com.ecommerce.admin.infrastructure.persistence.entity.BannerJpaEntity;
import com.ecommerce.admin.infrastructure.persistence.mapper.BannerPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final BannerJpaRepository jpaRepository;
    private final BannerPersistenceMapper mapper;

    @Override
    public Banner save(Banner banner) {
        BannerJpaEntity jpaEntity = mapper.toJpa(banner);
        BannerJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Banner> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Banner> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
