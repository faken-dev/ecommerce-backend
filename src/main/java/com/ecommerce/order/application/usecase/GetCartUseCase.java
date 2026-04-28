package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCartUseCase {

    private final CartRepository cartRepository;
    private final OrderApplicationMapper mapper;

    @Transactional(readOnly = true)
    public CartResponse execute(UUID buyerId) {
        Cart cart = cartRepository.getOrCreateByBuyerId(buyerId);
        return mapper.toCartResponse(cart);
    }
}
