package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

    Page<OrderJpaEntity> findBySellerIdAndStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status") String status,
            Pageable pageable);

    Page<OrderJpaEntity> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);

    Page<OrderJpaEntity> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    // Methods for Dashboard & Analytics
    long countByStatusAndDeletedAtIsNull(String status);

    @Query("SELECT COUNT(o) FROM OrderJpaEntity o WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :after AND o.deletedAt IS NULL")
    long countPaidOrdersAfter(@Param("after") Instant after);

    @Query("SELECT SUM(o.totalAmount) FROM OrderJpaEntity o WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :after AND o.deletedAt IS NULL")
    BigDecimal sumTotalAmountAfter(@Param("after") Instant after);

    @Query("SELECT SUM(o.totalAmount) FROM OrderJpaEntity o WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :start AND :end AND o.deletedAt IS NULL")
    BigDecimal sumTotalAmountBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT i.productId as productId, i.productName as productName, SUM(i.quantity) as totalSales, SUM(i.totalPrice) as totalRevenue " +
           "FROM OrderJpaEntity o JOIN o.items i " +
           "WHERE o.paymentStatus = 'PAID' AND o.deletedAt IS NULL " +
           "GROUP BY i.productId, i.productName ORDER BY totalSales DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("SELECT COUNT(o) > 0 FROM OrderJpaEntity o JOIN o.items i " +
           "WHERE o.buyerId = :buyerId AND i.productId = :productId AND o.status = 'DELIVERED' AND o.deletedAt IS NULL")
    boolean hasPurchasedProduct(@Param("buyerId") UUID buyerId, @Param("productId") UUID productId);

    // Seller Specific Methods
    long countBySellerIdAndDeletedAtIsNull(UUID sellerId);
    long countBySellerIdAndStatusAndDeletedAtIsNull(UUID sellerId, String status);

    @Query("SELECT COUNT(o) FROM OrderJpaEntity o WHERE o.sellerId = :sellerId AND o.paymentStatus = 'PAID' AND o.createdAt >= :after AND o.deletedAt IS NULL")
    long countBySellerIdAndCreatedAtAfter(@Param("sellerId") UUID sellerId, @Param("after") Instant after);

    @Query("SELECT SUM(o.totalAmount) FROM OrderJpaEntity o WHERE o.sellerId = :sellerId AND o.paymentStatus = 'PAID' AND o.createdAt >= :after AND o.deletedAt IS NULL")
    BigDecimal sumTotalAmountBySellerAfter(@Param("sellerId") UUID sellerId, @Param("after") Instant after);
}
