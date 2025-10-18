package dev.amirgol.stockmanager.service.product;

import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.dto.product.response.StockResponse;


import java.util.UUID;

/**
 * Product service interface defining inventory operations.
 *
 * <p>All implementations must ensure transactional integrity,
 * data consistency, and proper exception handling.</p>
 */
public interface ProductService {

    /**
     * Creates a new product with initial stock of 100.
     *
     * @param request product creation details
     * @return created product DTO
     */
    ProductDto createProduct(CreateProductRequest request);

    /**
     * Retrieves product information by ID.
     *
     * @param productId product UUID
     * @return product DTO
     */
    ProductDto getProduct(UUID productId);

    /**
     * Retrieves current stock information for a product.
     *
     * @param productId product UUID
     * @return stock response
     */
    StockResponse getProductStock(UUID productId);

    /**
     * Refills product stock by adding the specified amount.
     *
     * @param productId product UUID
     * @param amount quantity to add (must be positive)
     * @return updated stock response
     */
    StockResponse refillStock(UUID productId, int amount);

    /**
     * Purchases product by reducing stock.
     *
     * @param productId product UUID
     * @param quantity quantity to purchase
     * @return updated stock response
     */
    StockResponse buyProduct(UUID productId, int quantity);

    /**
     * Reserves product stock for a specified duration.
     *
     * @param productId product UUID
     * @param quantity quantity to reserve
     * @param durationMinutes reservation duration in minutes
     * @return updated stock response
     */
    StockResponse reserveProduct(UUID productId, int quantity, int durationMinutes);

    /**
     * Releases expired reservations across all products.
     *
     * @return count of released reservations
     */
    int releaseExpiredReservations();
}
