package com.ecommerce.user.infrastructure.persistence.mapper;

import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.infrastructure.persistence.entity.AddressJpaEntity;
import com.ecommerce.user.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDomainMapper {

    // ── UserProfile ──
    UserProfile toDomain(UserProfileJpaEntity jpa);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "gender", source = "gender")
    UserProfileJpaEntity toJpa(UserProfile domain);

    // ── Address ──
    Address toDomain(AddressJpaEntity jpa);


    @Mapping(target = "id", source = "id")
    AddressJpaEntity toJpa(Address domain);
}
