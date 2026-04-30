package com.ecommerce.catalog.infrastructure.persistence.mapper;

import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.entity.ProductImage;
import com.ecommerce.catalog.domain.entity.ProductVariant;
import com.ecommerce.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ecommerce.catalog.infrastructure.persistence.entity.ProductImageJpaEntity;
import com.ecommerce.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ecommerce.catalog.infrastructure.persistence.entity.ProductVariantJpaEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * Bidirectional mapper between Catalog domain entities and JPA entities.
 *
 * <ul>
 *   <li>IDs: uses {@code defaultExpression} so MapStruct generates a v7 UUID
 *       when the domain ID is null - matching {@code UuidCreator.getTimeOrderedEpoch()}.
 *   <li>Timestamps: ignored - JPA auditing populates them via
 *       {@code @CreatedDate}/{@code @LastModifiedDate}.
 *   <li>Tags: stored as TEXT in JPA (comma-separated), converted via static utility.
 *   <li>Parent refs on child entities (variant/image): passed via {@code @Context}.
 * </ul>
 */
@Mapper(componentModel = "spring",
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CatalogDomainMapper {

    // - Category -------------

    Category toCategory(CategoryJpaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CategoryJpaEntity toCategoryJpa(Category domain);

    // - Product --------------

    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? Product.Status.valueOf(entity.getStatus()) : Product.Status.DRAFT)")
    @Mapping(target = "visibility", expression = "java(entity.getVisibility() != null ? Product.Visibility.valueOf(entity.getVisibility()) : Product.Visibility.SHOP)")
    @Mapping(target = "tags", expression = "java(TagsConverter.stringToList(entity.getTags()))")
    Product toProduct(ProductJpaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"DRAFT\")")
    @Mapping(target = "visibility", expression = "java(domain.getVisibility() != null ? domain.getVisibility().name() : \"SHOP\")")
    @Mapping(target = "tags", expression = "java(TagsConverter.listToString(domain.getTags()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductJpaEntity toProductJpa(Product domain);

    // - ProductVariant ----------

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    ProductVariant toVariant(ProductVariantJpaEntity entity);

    /**
     * Caller is responsible for ensuring the parent {@code product} JPA entity
     * is already persisted (required for FK integrity). The {@code product}
     * argument is passed via the {@code @Context} parameter at the call-site.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", expression = "java(product)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductVariantJpaEntity toVariantJpa(ProductVariant domain,
                                       @Context ProductJpaEntity product);

    // - ProductImage -----------

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    ProductImage toImage(ProductImageJpaEntity entity);

    /**
     * Caller is responsible for ensuring the parent {@code product} JPA entity
     * is already persisted (required for FK integrity).
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", expression = "java(product)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductImageJpaEntity toImageJpa(ProductImage domain,
                                     @Context ProductJpaEntity product);

    // - Collections -----------

    List<ProductVariant> toVariantList(List<ProductVariantJpaEntity> entities);

    List<ProductImage> toImageList(List<ProductImageJpaEntity> entities);

    @AfterMapping
    default void linkBackReferences(@MappingTarget ProductJpaEntity target) {
        if (target.getImages() != null) {
            target.getImages().forEach(img -> img.setProduct(target));
        }
        if (target.getVariants() != null) {
            target.getVariants().forEach(v -> v.setProduct(target));
        }
    }
}
