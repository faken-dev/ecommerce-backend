package com.ecommerce.admin.domain.repository;

import com.ecommerce.admin.domain.entity.Banner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BannerRepository {
    Banner save(Banner banner);
    Optional<Banner> findById(UUID id);
    List<Banner> findAll();
    void deleteById(UUID id);
}
