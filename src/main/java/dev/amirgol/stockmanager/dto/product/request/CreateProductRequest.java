package dev.amirgol.stockmanager.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new product.
 *
 * <p>This record is immutable and validated using Jakarta Bean Validation.
 * It represents the required fields for creating a new {@code Product}.</p>
 */
@Builder
public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
        BigDecimal price
) {}
