package dev.amirgol.stockmanager.dto.product;

import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Product entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "availableStock", expression = "java(product.getAvailableStock())")
    ProductDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", constant = "100")
    @Mapping(target = "reservedUntil", ignore = true)
    @Mapping(target = "reservedQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);
}