package com.ecommerce.catalog.application.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.domain.entity.*;

@Mapper(componentModel = "spring")
public interface CatalogApplicationMapper {

    @Mapping(target = "status", expression = "java(product.getStatus().name())")
    @Mapping(target = "visibility", expression = "java(product.getVisibility().name())")
    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    @Mapping(target = "imageUrl", expression = "java(product.getPrimaryImageUrl())")
    @Mapping(target = "descriptionHtml", source = "description") // Use description as HTML for now
    @Mapping(target = "images", source = "images")
    @Mapping(target = "stockQuantity", ignore = true)
    @Mapping(target = "lowStockThreshold", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "threeDModelUrl", source = "threeDModelUrl")
    ProductResponse toProductResponse(Product product);
    
    ProductImageResponse toProductImageResponse(ProductImage image);

    @Mapping(target = "status", expression = "java(product.getStatus().name())")
    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    @Mapping(target = "imageUrl", expression = "java(product.getPrimaryImageUrl())")
    @Mapping(target = "stockQuantity", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    ProductSummaryResponse toProductSummaryResponse(Product product);

    @Mapping(target = "parentName", ignore = true)
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "children", source = "children")
    CategoryTreeResponse toCategoryTreeResponse(Category category, List<CategoryTreeResponse> children);
}
