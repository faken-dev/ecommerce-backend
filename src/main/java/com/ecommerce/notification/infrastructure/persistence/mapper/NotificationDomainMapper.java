package com.ecommerce.notification.infrastructure.persistence.mapper;

import com.ecommerce.notification.domain.entity.InAppNotification;
import com.ecommerce.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationDomainMapper {

    @Mapping(target = "version", ignore = true)
    NotificationJpaEntity toJpaEntity(InAppNotification domain);

    InAppNotification toDomain(NotificationJpaEntity jpa);

    @Mapping(target = "version", ignore = true)
    void updateJpaEntity(InAppNotification domain, @MappingTarget NotificationJpaEntity jpa);
}
