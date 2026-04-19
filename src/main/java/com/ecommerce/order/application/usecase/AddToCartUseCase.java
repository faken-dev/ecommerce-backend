package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.AddToCartCommand;
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
public class AddToCartUseCase {

    private final CartRepository cartRepository;
    private final OrderApplicationMapper mapper;

    @Transactional
    public CartResponse execute(AddToCartCommand cmd, UUID buyerId) {
        // Guard: at least one identifier must be present
        if (cmd.productId() == null) {
            throw new IllegalArgumentException("productId is required");
        }

        Cart cart = cartRepository.getOrCreateByBuyerId(buyerId);
        cart.addItem(
                cmd.productId(),
                cmd.variantId(),
                cmd.quantity(),
                cmd.unitPrice()
        );
        Cart saved = cartRepository.save(cart);
        return mapper.toCartResponse(saved);
    }
}
