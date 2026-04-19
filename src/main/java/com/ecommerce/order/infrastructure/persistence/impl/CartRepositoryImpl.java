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

import java.util.Optional;

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
        // directly — changes will be auto-flushed at transaction end.
        // The @Version field ensures optimistic locking.
        if (entity.getId() != null) {
            Optional<CartJpaEntity> managed = cartJpaRepository.findById(entity.getId());
            if (managed.isPresent()) {
                CartJpaEntity existing = managed.get();
                existing.setBuyerId(entity.getBuyerId());
                // Sync items: clear stale ones, add new ones; orphanRemoval handles deletion
                existing.getItems().clear();
                entity.getItems().forEach(existing::addItem);
                // Map back: MapStruct maps scalar fields; wire items manually
                Cart result = mapper.toCart(existing);
                result.getItems().clear();
                for (CartItemJpaEntity savedItem : existing.getItems()) {
                    result.getItems().add(mapper.toCartItem(savedItem));
                }
                return result;
            }
        }

        // New cart — persist via cascade (items saved automatically)
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
    public Cart getOrCreateByBuyerId(java.util.UUID buyerId) {
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
    public Cart findByBuyerId(java.util.UUID buyerId) {
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
