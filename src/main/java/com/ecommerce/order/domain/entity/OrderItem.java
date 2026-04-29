package com.ecommerce.order.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrderItem extends AuditableEntity {

    private UUID orderId;
    private UUID productId;
    private UUID categoryId;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private UUID variantId;
    private String variantTitle;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private int refundedQuantity;
    private BigDecimal refundedAmount;

    public static OrderItem create(UUID orderId, UUID productId, String productName,
            String productSku, String productImageUrl,
            UUID variantId, String variantTitle,
            int quantity, BigDecimal unitPrice) {
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return OrderItem.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .orderId(orderId)
                .productId(productId)
                .productName(productName)
                .productSku(productSku)
                .productImageUrl(productImageUrl)
                .variantId(variantId)
                .variantTitle(variantTitle)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(total)
                .discountAmount(BigDecimal.ZERO)
                .refundedQuantity(0)
                .refundedAmount(BigDecimal.ZERO)
                .build();
    }

    public void applyDiscount(BigDecimal amount) {
        this.discountAmount = amount;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(amount);
    }

    public void recordRefund(int qty, BigDecimal amount) {
        this.refundedQuantity += qty;
        this.refundedAmount = this.refundedAmount.add(amount);
    }

    public boolean isFullyRefunded() {
        return refundedQuantity >= quantity;
    }
}
