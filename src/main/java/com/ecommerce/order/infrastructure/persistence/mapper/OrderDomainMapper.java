package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.entity.*;
import com.ecommerce.order.infrastructure.persistence.entity.*;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bidirectional mapper between Order domain entities and JPA entities.
 *
 * <ul>
 *   <li>Timestamps: ignored Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â JPA auditing populates them via
 *       {@code @CreatedDate}/{@code @LastModifiedDate}.
 *   <li>StatusHistory entries are kept in-memory on the domain side and persisted
 *       via {@link OrderStatusHistoryJpaEntity} separately (not via this mapper).
 *   <li>Parent references on child entities (item Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ order, cartItem Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ cart):
 *       handled via {@code @Context} parameters at the call-site.
 * </ul>
 */
@Mapper(componentModel = "spring",
       imports = { OrderStatus.class, PaymentStatus.class, Instant.class },
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderDomainMapper {

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Order Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    @Mapping(target = "status", expression = "java(OrderStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "paymentStatus", expression = "java(PaymentStatus.valueOf(entity.getPaymentStatus()))")
    Order toOrder(OrderJpaEntity entity);

    /** Reusable method to map JPA items Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ domain items. */
    List<OrderItem> toOrderItemList(List<OrderItemJpaEntity> entities);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "paymentStatus", expression = "java(domain.getPaymentStatus().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderJpaEntity toOrderJpa(Order domain);

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ OrderItem Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

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

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Cart Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    Cart toCart(CartJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "items", expression = "java(toCartItemJpaList(domain.getItems(), null))")
    CartJpaEntity toCartJpa(Cart domain);

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ CartItem Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    /**
     * Converts JPA entity Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ domain CartItem (inner record in Cart).
     * The {@code cart} context is ignored here since CartItem has no cart field.
     */
    Cart.CartItem toCartItem(CartItemJpaEntity entity,
                             @Context CartJpaEntity cart);

    /**
     * Converts domain CartItem Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ JPA entity.
     * The {@code cart} context must be pre-loaded and already persisted.
     */
    @Mapping(target = "cart", expression = "java(cart)")
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    CartItemJpaEntity toCartItemJpa(Cart.CartItem domain,
                                    @Context CartJpaEntity cart);

    // Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬ Collections Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬Ä‚Â¢Ă¢â‚¬ÂĂ¢â€Â¬

    List<Cart.CartItem> toCartItemList(List<CartItemJpaEntity> entities);

    default List<CartItemJpaEntity> toCartItemJpaList(List<Cart.CartItem> domains, @Context CartJpaEntity cart) {
        if (domains == null) return null;
        return domains.stream()
                .map(d -> toCartItemJpa(d, cart))
                .collect(Collectors.toList());
    }

    Cart.CartItem toCartItem(CartItemJpaEntity entity);

    @AfterMapping
    default void linkItems(@MappingTarget CartJpaEntity entity) {
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> item.setCart(entity));
        }
    }
}
