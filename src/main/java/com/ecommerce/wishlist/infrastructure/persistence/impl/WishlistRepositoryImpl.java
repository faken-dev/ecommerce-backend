package com.ecommerce.wishlist.infrastructure.persistence.impl;

import com.ecommerce.wishlist.domain.entity.WishlistItem;
import com.ecommerce.wishlist.domain.repository.WishlistRepository;
import com.ecommerce.wishlist.infrastructure.persistence.entity.WishlistItemJpaEntity;
import com.ecommerce.wishlist.infrastructure.persistence.mapper.WishlistDomainMapper;
import com.ecommerce.wishlist.infrastructure.persistence.repository.WishlistJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WishlistRepositoryImpl implements WishlistRepository {

    private final WishlistJpaRepository jpaRepository;
    private final WishlistDomainMapper mapper;

    @Override
    @Transactional
    public WishlistItem save(WishlistItem item) {
        WishlistItemJpaEntity jpa = mapper.toJpa(item);
        return mapper.toDomain(jpaRepository.save(jpa));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID productId) {
        jpaRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId) {
        return jpaRepository.findByUserIdAndProductId(userId, productId)
                .map(mapper::toDomain);
    }

    @Override
    public List<WishlistItem> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return jpaRepository.existsByUserIdAndProductId(userId, productId);
    }
}
