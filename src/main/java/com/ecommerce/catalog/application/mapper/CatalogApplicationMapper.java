package com.ecommerce.catalog.application.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.catalog.application.dto.CategoryResponse;
import com.ecommerce.catalog.application.dto.CategoryTreeResponse;
import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.entity.Product;

@Mapper(componentModel = "spring")
public interface CatalogApplicationMapper {

    @Mapping(target = "status", expression = "java(product.getStatus().name())")
    @Mapping(target = "visibility", expression = "java(product.getVisibility().name())")
    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "status", expression = "java(product.getStatus().name())")
    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    ProductSummaryResponse toProductSummaryResponse(Product product);

   
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "children", source = "children")
    CategoryTreeResponse toCategoryTreeResponse(Category category, List<CategoryTreeResponse> children);
}