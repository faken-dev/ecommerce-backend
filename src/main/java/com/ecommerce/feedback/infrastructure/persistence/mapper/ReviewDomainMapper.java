package com.ecommerce.feedback.infrastructure.persistence.mapper;

import com.ecommerce.feedback.domain.entity.Review;
import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewDomainMapper {

    @Mapping(target = "version", ignore = true)
    ReviewJpaEntity toJpaEntity(Review domain);

    Review toDomain(ReviewJpaEntity jpa);
}
