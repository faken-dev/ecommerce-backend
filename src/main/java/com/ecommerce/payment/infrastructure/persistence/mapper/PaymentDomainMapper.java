package com.ecommerce.payment.infrastructure.persistence.mapper;

import com.ecommerce.payment.domain.entity.*;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.ecommerce.payment.infrastructure.persistence.entity.RefundJpaEntity;
import java.util.List;
import org.mapstruct.*;

/**
 * Bidirectional mapper between Payment domain entities and JPA entities.
 *
 * <ul>
 *   <li>Timestamps: ignored - JPA auditing populates them via
 *       {@code @CreatedDate}/{@code @LastModifiedDate}.
 *   <li>Refunds are filtered to exclude soft-deleted (deletedAt != null).
 *   <li>Parent references (refund → payment): handled via {@code @Context} at call-site.
 * </ul>
 */
@Mapper(componentModel = "spring",
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
       imports = {PaymentProvider.class, PaymentMethodType.class, PaymentStatus.class})
public interface PaymentDomainMapper {

    // - Payment ------------

    @Mapping(target = "provider", expression = "java(PaymentProvider.valueOf(entity.getProvider()))")
    @Mapping(target = "methodType", expression = "java(PaymentMethodType.valueOf(entity.getMethodType()))")
    @Mapping(target = "status", expression = "java(PaymentStatus.valueOf(entity.getStatus()))")
    Payment toDomain(PaymentJpaEntity entity);

    @Mapping(target = "provider", expression = "java(domain.getProvider().name())")
    @Mapping(target = "methodType", expression = "java(domain.getMethodType().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    PaymentJpaEntity toJpa(Payment domain);

    // - Refund ------------

    /**
     * Reads {@code paymentId} from the pre-loaded parent entity.
     */
    @Mapping(target = "paymentId", expression = "java(entity.getPayment().getId())")
    Refund toRefundDomain(RefundJpaEntity entity);

    /**
     * Converts domain → JPA. The {@code payment} context must be pre-loaded.
     */
    @Mapping(target = "payment", expression = "java(payment)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RefundJpaEntity toRefundJpa(Refund domain,
                                 @Context PaymentJpaEntity payment);

    // - Collections ----------

    /**
     * Maps all JPA entities; callers filter out soft-deleted (deletedAt != null)
     * at the call-site.
     */
    List<Refund> toRefundList(List<RefundJpaEntity> entities);
}
