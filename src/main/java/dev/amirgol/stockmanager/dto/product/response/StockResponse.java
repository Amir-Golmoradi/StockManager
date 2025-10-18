package dev.amirgol.stockmanager.dto.product.response;

import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for stock-related operations.
 *
 * <p>This record is immutable and represents stock details for a given product.</p>
 */
@Builder
public record StockResponse(
        UUID productId,
        Integer totalStock,
        Integer availableStock,
        Integer reservedQuantity
) {

    /**
     * Creates a new {@code StockResponse} instance.
     *
     * @param productId       unique product identifier
     * @param totalStock      total stock quantity
     * @param availableStock  available (non-reserved) stock quantity
     * @param reservedQuantity quantity currently reserved
     * @return a new immutable {@code StockResponse}
     */
    public static StockResponse of(UUID productId, Integer totalStock, Integer availableStock, Integer reservedQuantity) {
        return new StockResponse(productId, totalStock, availableStock, reservedQuantity);
    }
}
