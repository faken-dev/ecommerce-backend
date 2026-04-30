package com.ecommerce.admin.application.usecase;

import com.ecommerce.admin.infrastructure.persistence.repository.BannerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteBannerUseCase {
    private final BannerJpaRepository bannerJpaRepository;

    @Transactional
    public void execute(UUID id) {
        bannerJpaRepository.deleteById(id);
    }
}
