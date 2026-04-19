package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.id = :orderId AND o.deletedAt IS NULL")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("orderId") UUID orderId);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.buyerId = :buyerId AND o.deletedAt IS NULL")
    Page<OrderJpaEntity> findByBuyerId(@Param("buyerId") UUID buyerId, Pageable pageable);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.sellerId = :sellerId AND o.deletedAt IS NULL")
    Page<OrderJpaEntity> findBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.buyerId = :buyerId AND o.status = :status AND o.deletedAt IS NULL")
    Page<OrderJpaEntity> findByBuyerIdAndStatus(
            @Param("buyerId") UUID buyerId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.sellerId = :sellerId AND o.status = :status AND o.deletedAt IS NULL")
    Page<OrderJpaEntity> findBySellerIdAndStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status") String status,
            Pageable pageable);

    boolean existsByIdAndDeletedAtIsNull(UUID id);
}
