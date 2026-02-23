package com.shopnest.mapper;

import com.shopnest.dto.request.CreateProductRequest;
import com.shopnest.dto.request.UpdateProductRequest;
import com.shopnest.dto.response.ProductResponse;
import com.shopnest.entity.ProductEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    // Entity → Response DTO
    @Mapping(source = "category.id",      target = "categoryId")
    @Mapping(source = "category.name",    target = "categoryName")
    @Mapping(source = "seller.id",        target = "sellerId")
    @Mapping(source = "seller.firstName", target = "sellerName")
    ProductResponse toResponse(ProductEntity entity);

    // CreateRequest → Entity
    @BeanMapping(ignoreByDefault = true)    // ← only map explicitly listed fields
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price",       source = "price")
    @Mapping(target = "stockQty",    source = "stockQty")
    @Mapping(target = "imageUrl",    source = "imageUrl")
    @Mapping(target = "type",        source = "type")
    @Mapping(target = "brand",       source = "brand")
    ProductEntity toEntity(CreateProductRequest request);

    // UpdateRequest → existing Entity (partial update)
    @BeanMapping(ignoreByDefault = true)    // ← only map explicitly listed fields
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price",       source = "price")
    @Mapping(target = "stockQty",    source = "stockQty")
    @Mapping(target = "imageUrl",    source = "imageUrl")
    @Mapping(target = "type",        source = "type")
    @Mapping(target = "brand",       source = "brand")
    @Mapping(target = "active",      source = "active")
    void updateEntity(@MappingTarget ProductEntity entity, UpdateProductRequest request);
}