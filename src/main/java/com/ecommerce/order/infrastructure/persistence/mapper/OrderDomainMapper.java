package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.entity.*;
import com.ecommerce.order.infrastructure.persistence.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * Bidirectional mapper between Order domain entities and JPA entities.
 *
 * <ul>
 *   <li>Timestamps: ignored — JPA auditing populates them via
 *       {@code @CreatedDate}/{@code @LastModifiedDate}.
 *   <li>StatusHistory entries are kept in-memory on the domain side and persisted
 *       via {@link OrderStatusHistoryJpaEntity} separately (not via this mapper).
 *   <li>Parent references on child entities (item → order, cartItem → cart):
 *       handled via {@code @Context} parameters at the call-site.
 * </ul>
 */
@Mapper(componentModel = "spring",
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderDomainMapper {

    // ── Order ───────────────────────────────────────────────────────────────

    @Mapping(target = "status", expression = "java(com.ecommerce.order.domain.entity.OrderStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "paymentStatus", expression = "java(com.ecommerce.order.domain.entity.PaymentStatus.valueOf(entity.getPaymentStatus()))")
    Order toOrder(OrderJpaEntity entity);

    /** Reusable method to map JPA items → domain items. */
    List<OrderItem> toOrderItemList(List<OrderItemJpaEntity> entities);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "paymentStatus", expression = "java(domain.getPaymentStatus().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderJpaEntity toOrderJpa(Order domain);

    // ── OrderItem ──────────────────────────────────────────────────────────

    /**
     * Note: {@code orderId} is read from the pre-loaded parent entity via
     * {@code entity.getOrder().getId()}.
     */
    @Mapping(target = "orderId", expression = "java(entity.getOrder().getId())")
    OrderItem toOrderItem(OrderItemJpaEntity entity);

    /**
     * Caller must call {@link OrderJpaEntity#addItem(OrderItemJpaEntity)} to wire
     * the bidirectional relationship.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderItemJpaEntity toOrderItemJpa(OrderItem domain);

    // ── Cart ───────────────────────────────────────────────────────────────

    Cart toCart(CartJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartJpaEntity toCartJpa(Cart domain);

    // ── CartItem ──────────────────────────────────────────────────────────

    /**
     * Converts JPA entity → domain CartItem (inner record in Cart).
     * The {@code cart} context is ignored here since CartItem has no cart field.
     */
    Cart.CartItem toCartItem(CartItemJpaEntity entity,
                             @Context CartJpaEntity cart);

    /**
     * Converts domain CartItem → JPA entity.
     * The {@code cart} context must be pre-loaded and already persisted.
     */
    @Mapping(target = "cart", expression = "java(cart)")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    CartItemJpaEntity toCartItemJpa(Cart.CartItem domain,
                                    @Context CartJpaEntity cart);

    // ── Collections ───────────────────────────────────────────────────────

    List<Cart.CartItem> toCartItemList(List<CartItemJpaEntity> entities);

    /**
     * Maps a single JPA entity → domain CartItem.
     * @param entity CartItemJpaEntity — the item to map
     * @param cart    ignored (required by MapStruct overload resolution)
     */
    Cart.CartItem toCartItem(CartItemJpaEntity entity);
}
