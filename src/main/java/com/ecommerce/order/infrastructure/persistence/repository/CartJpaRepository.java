package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {

    @Query("SELECT c FROM CartJpaEntity c LEFT JOIN FETCH c.items WHERE c.buyerId = :buyerId")
    Optional<CartJpaEntity> findByBuyerIdWithItems(@Param("buyerId") UUID buyerId);

    Optional<CartJpaEntity> findByBuyerId(UUID buyerId);
}
