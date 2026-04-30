package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.command.AddToCartCommand;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.application.port.ProductQueryPort;
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
    private final ProductQueryPort productQueryPort;

    @Transactional
    public CartResponse execute(AddToCartCommand cmd, UUID buyerId) {
        if (cmd.productId() == null) {
            throw new IllegalArgumentException("productId is required");
        }

        var productInfo = productQueryPort.getProductInfo(cmd.productId());
        if (productInfo == null) {
            throw new IllegalArgumentException("Product not found");
        }

        Cart cart = cartRepository.getOrCreateByBuyerId(buyerId);
        cart.addItem(
                cmd.productId(),
                productInfo.sellerId(),
                productInfo.name(),
                productInfo.imageUrl(),
                cmd.variantId(),
                null, // variantTitle - could be enriched if needed
                cmd.quantity(),
                cmd.unitPrice()
        );
        Cart saved = cartRepository.save(cart);
        return mapper.toCartResponse(saved);
    }
}
