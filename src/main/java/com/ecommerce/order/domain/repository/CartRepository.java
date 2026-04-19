package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.entity.Cart;

import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    void delete(Cart cart);

    Cart getOrCreateByBuyerId(UUID buyerId);

    Cart findByBuyerId(UUID buyerId);
}
