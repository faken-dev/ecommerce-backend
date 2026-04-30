package com.ecommerce.feedback.infrastructure.persistence.mapper;

import com.ecommerce.feedback.domain.entity.ReviewComment;
import com.ecommerce.feedback.infrastructure.persistence.entity.ReviewCommentJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewCommentDomainMapper {

    @Mapping(target = "version", ignore = true)
    ReviewCommentJpaEntity toJpaEntity(ReviewComment domain);

    ReviewComment toDomain(ReviewCommentJpaEntity jpa);
}
