package com.ecommerce.voucher.infrastructure.persistence.repository;

import com.ecommerce.voucher.domain.entity.UserVoucher;
import com.ecommerce.voucher.domain.repository.UserVoucherRepository;
import com.ecommerce.voucher.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ecommerce.voucher.infrastructure.persistence.mapper.VoucherDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class UserVoucherRepositoryImpl implements UserVoucherRepository {

    private final UserVoucherJpaRepository jpaRepository;
    private final VoucherDomainMapper mapper;

    @Override
    public UserVoucher save(UserVoucher userVoucher) {
        UserVoucherJpaEntity jpa = mapper.toJpa(userVoucher);
        return mapper.toDomain(jpaRepository.save(jpa));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserVoucher> findByUserIdAndVoucherId(UUID userId, UUID voucherId) {
        return jpaRepository.findByUserIdAndVoucherId(userId, voucherId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucher> findByUserId(UUID userId, Pageable pageable) {
        Page<UserVoucherJpaEntity> page = jpaRepository.findByUserId(userId, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucher> findActiveVouchersByUserId(UUID userId, Pageable pageable) {
        Page<UserVoucherJpaEntity> page = jpaRepository.findActiveVouchersByUserId(userId, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId) {
        return jpaRepository.existsByUserIdAndVoucherId(userId, voucherId);
    }
}
