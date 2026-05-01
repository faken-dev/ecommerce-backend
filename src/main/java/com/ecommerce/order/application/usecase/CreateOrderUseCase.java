package com.ecommerce.order.application.usecase;

import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.order.application.command.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.application.port.InventoryQueryPort;
import com.ecommerce.order.application.port.VoucherQueryPort;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryQueryPort inventoryQueryPort;
    private final VoucherQueryPort voucherQueryPort;
    private final OrderApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(CreateOrderCommand cmd, UUID buyerId,
                                 String ipAddress, String userAgent) {

        // 1. Stock & Product Validation
        List<OrderItem> items = cmd.items().stream()
                .map(itemCmd -> {
                    Product product = productRepository.findById(itemCmd.productId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                    // Verify seller ownership
                    if (!product.getSellerId().equals(cmd.sellerId())) {
                        throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
                    }

                    // Check stock
                    int available = inventoryQueryPort.getAvailableStock(itemCmd.productId(), itemCmd.variantId());
                    if (available < itemCmd.quantity()) {
                        throw new BusinessException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK);
                    }

                    return OrderItem.create(
                            null,
                            itemCmd.productId(),
                            product.getCategoryId(),
                            itemCmd.productName(),
                            itemCmd.productSku(),
                            itemCmd.productImageUrl(),
                            itemCmd.variantId(),
                            itemCmd.variantTitle(),
                            itemCmd.quantity(),
                            itemCmd.unitPrice()
                    );
                })
                .toList();

        // 2. Voucher Validation (Server-side calculation for security)
        BigDecimal calculatedDiscount = BigDecimal.ZERO;
        if (cmd.voucherCode() != null && !cmd.voucherCode().isBlank()) {
            Set<UUID> productIds = items.stream().map(OrderItem::getProductId).collect(Collectors.toSet());
            Set<UUID> categoryIds = items.stream().map(OrderItem::getCategoryId).collect(Collectors.toSet());

            VoucherQueryPort.DiscountInfo discountInfo = voucherQueryPort.calculateDiscount(
                    cmd.voucherCode(), buyerId, cmd.subtotal(), cmd.shippingFee(), productIds, categoryIds);

            if (!discountInfo.applicable()) {
                throw new BusinessException(ErrorCode.VOUCHER_NOT_APPLICABLE, discountInfo.reason());
            }
            calculatedDiscount = discountInfo.discountAmount();
        } else {
            calculatedDiscount = cmd.discountAmount() != null ? cmd.discountAmount() : BigDecimal.ZERO;
        }

        // 3. Create Order
        Order order = Order.createOrder(
                buyerId,
                cmd.sellerId(),
                cmd.shippingAddressId(),
                items,
                cmd.subtotal(),
                cmd.shippingFee(),
                cmd.taxAmount(),
                calculatedDiscount,
                cmd.currency(),
                ipAddress,
                userAgent
        );

        if (cmd.buyerNote() != null && !cmd.buyerNote().isBlank()) {
            order.setBuyerNote(cmd.buyerNote());
        }
        
        if (cmd.voucherCode() != null) {
            order.setAppliedVoucherCode(cmd.voucherCode());
        }

        if (cmd.paymentMethod() != null) {
            order.setPaymentMethod(cmd.paymentMethod());
        }

        Order saved = orderRepository.save(order);

        // 4. Cleanup & Events
        eventPublisher.publish(saved.toCreatedEvent());

        Cart cart = cartRepository.findByBuyerId(buyerId);
        if (cart != null) {
            cartRepository.delete(cart);
        }

        return mapper.toOrderResponse(saved);
    }
}
