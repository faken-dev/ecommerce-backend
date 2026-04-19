package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final OrderApplicationMapper mapper;

    @Transactional
    public CartResponse execute(UUID productId, UUID variantId, UUID buyerId) {
        Cart cart = cartRepository.findByBuyerId(buyerId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cart.removeItem(productId, variantId);
        Cart saved = cartRepository.save(cart);
        return mapper.toCartResponse(saved);
    }
}