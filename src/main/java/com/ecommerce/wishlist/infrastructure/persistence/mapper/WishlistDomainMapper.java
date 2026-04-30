package com.ecommerce.wishlist.infrastructure.persistence.mapper;

import com.ecommerce.wishlist.domain.entity.WishlistItem;
import com.ecommerce.wishlist.infrastructure.persistence.entity.WishlistItemJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WishlistDomainMapper {
    WishlistItem toDomain(WishlistItemJpaEntity jpa);
    WishlistItemJpaEntity toJpa(WishlistItem domain);
}
