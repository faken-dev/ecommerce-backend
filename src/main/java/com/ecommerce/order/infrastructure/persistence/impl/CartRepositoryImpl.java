package com.ecommerce.order.infrastructure.persistence.impl;

import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.order.infrastructure.persistence.entity.CartJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.CartItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.mapper.OrderDomainMapper;
import com.ecommerce.order.infrastructure.persistence.repository.CartJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final OrderDomainMapper mapper;

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity entity = mapper.toCartJpa(cart);

        // If cart already exists, update the MANAGED instance (returned by findById)
        // directly - changes will be auto-flushed at transaction end.
        // The @Version field ensures optimistic locking.
        if (entity.getId() != null) {
            Optional<CartJpaEntity> managed = cartJpaRepository.findById(entity.getId());
            if (managed.isPresent()) {
                CartJpaEntity existing = managed.get();
                existing.setBuyerId(entity.getBuyerId());
                
                // Sync items: instead of clear & add, update existing or add new
                // This prevents NonUniqueObjectException and correctly persists quantity updates
                List<CartItemJpaEntity> existingItems = existing.getItems();
                List<CartItemJpaEntity> newItems = entity.getItems();

                // 1. Remove items not in the new list
                existingItems.removeIf(ei -> newItems.stream()
                        .noneMatch(ni -> ni.getId() != null && ni.getId().equals(ei.getId())));

                // 2. Update existing or add new
                for (CartItemJpaEntity ni : newItems) {
                    Optional<CartItemJpaEntity> found = existingItems.stream()
                            .filter(ei -> (ei.getId() != null && ei.getId().equals(ni.getId())) || 
                                         (ei.getProductId().equals(ni.getProductId()) && 
                                          Objects.equals(ei.getVariantId(), ni.getVariantId())))
                            .findFirst();

                    if (found.isPresent()) {
                        CartItemJpaEntity ei = found.get();
                        ei.setQuantity(ni.getQuantity());
                        ei.setUnitPrice(ni.getUnitPrice());
                        ei.setProductName(ni.getProductName());
                        ei.setProductImageUrl(ni.getProductImageUrl());
                        ei.setVariantTitle(ni.getVariantTitle());
                        ei.setSellerId(ni.getSellerId());
                    } else {
                        existing.addItem(ni);
                    }
                }

                CartJpaEntity saved = cartJpaRepository.saveAndFlush(existing);
                Cart result = mapper.toCart(saved);
                result.getItems().clear();
                saved.getItems().forEach(i -> result.getItems().add(mapper.toCartItem(i)));
                return result;
            }
        }

        // New cart - persist via cascade (items saved automatically)
        CartJpaEntity saved = cartJpaRepository.save(entity);
        // Map back: MapStruct maps scalar fields; wire items manually
        Cart result = mapper.toCart(saved);
        result.getItems().clear();
        for (CartItemJpaEntity savedItem : saved.getItems()) {
            result.getItems().add(mapper.toCartItem(savedItem));
        }
        return result;
    }

    @Override
    public void delete(Cart cart) {
        cartJpaRepository.findByBuyerId(cart.getBuyerId())
                .ifPresent(cartJpaRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getOrCreateByBuyerId(UUID buyerId) {
        return cartJpaRepository.findByBuyerIdWithItems(buyerId)
                .map(entity -> {
                    Cart cart = mapper.toCart(entity);
                    cart.getItems().clear();
                    entity.getItems().forEach(i -> cart.getItems().add(mapper.toCartItem(i)));
                    return cart;
                })
                .orElseGet(() -> {
                    Cart newCart = Cart.createCart(buyerId);
                    return save(newCart);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Cart findByBuyerId(UUID buyerId) {
        return cartJpaRepository.findByBuyerIdWithItems(buyerId)
                .map(entity -> {
                    Cart cart = mapper.toCart(entity);
                    cart.getItems().clear();
                    entity.getItems().forEach(i -> cart.getItems().add(mapper.toCartItem(i)));
                    return cart;
                })
                .orElse(null);
    }
}
