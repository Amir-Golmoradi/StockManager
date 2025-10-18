package dev.amirgol.stockmanager.dto.product.request;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Product responses.
 */
@Builder
public record ProductDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        int availableStock,
        LocalDateTime reservedUntil,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}