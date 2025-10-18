package dev.amirgol.stockmanager.service.product;

import dev.amirgol.stockmanager.dto.product.ProductMapper;
import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.dto.product.response.StockResponse;
import dev.amirgol.stockmanager.exception.InsufficientStockException;
import dev.amirgol.stockmanager.exception.ProductNotFoundException;
import dev.amirgol.stockmanager.model.Product;
import dev.amirgol.stockmanager.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of {@link ProductService}.
 *
 * <p>Manages product inventory operations with strict JPA and transactional consistency.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.name());
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        log.debug("Fetching product with id: {}", productId);
        Product product = findProductById(productId);
        product.releaseExpiredReservation();
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getProductStock(UUID productId) {
        log.debug("Fetching stock for product id: {}", productId);
        Product product = findProductById(productId);
        product.releaseExpiredReservation();

        return new StockResponse(
                product.getId(),
                product.getStock(),
                product.getAvailableStock(),
                product.getReservedQuantity()
        );
    }

    @Override
    @Transactional
    public StockResponse refillStock(UUID productId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Refill amount must be positive");
        }

        log.info("Refilling stock for product id: {} with amount: {}", productId, amount);
        Product product = findProductById(productId);
        product.releaseExpiredReservation();

        int newStock = product.getStock() + amount;
        product.setStock(newStock);

        Product updatedProduct = productRepository.save(product);
        log.info("Stock refilled successfully. New stock: {}", newStock);

        return new StockResponse(
                updatedProduct.getId(),
                updatedProduct.getStock(),
                updatedProduct.getAvailableStock(),
                updatedProduct.getReservedQuantity()
        );
    }

    @Override
    @Transactional
    public StockResponse buyProduct(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be positive");
        }

        log.info("Processing purchase for product id: {} with quantity: {}", productId, quantity);
        Product product = findProductById(productId);
        product.releaseExpiredReservation();

        int availableStock = product.getAvailableStock();
        if (availableStock < quantity) {
            log.warn("Insufficient stock. Available: {}, Requested: {}", availableStock, quantity);
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", availableStock, quantity)
            );
        }

        if (product.getReservedQuantity() != null && quantity <= product.getReservedQuantity()) {
            product.setReservedQuantity(null);
            product.setReservedUntil(null);
        }

        int newStock = product.getStock() - quantity;
        product.setStock(newStock);

        Product updatedProduct = productRepository.save(product);
        log.info("Purchase completed successfully. New stock: {}", newStock);

        return new StockResponse(
                updatedProduct.getId(),
                updatedProduct.getStock(),
                updatedProduct.getAvailableStock(),
                updatedProduct.getReservedQuantity()
        );
    }

    @Override
    @Transactional
    public StockResponse reserveProduct(UUID productId, int quantity, int durationMinutes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Reservation duration must be positive");
        }

        log.info("Processing reservation for product id: {} with quantity: {} for {} minutes",
                productId, quantity, durationMinutes);

        Product product = findProductById(productId);
        product.releaseExpiredReservation();

        int availableStock = product.getAvailableStock();
        if (availableStock < quantity) {
            log.warn("Insufficient stock for reservation. Available: {}, Requested: {}", availableStock, quantity);
            throw new InsufficientStockException(
                    String.format("Insufficient stock for reservation. Available: %d, Requested: %d",
                            availableStock, quantity)
            );
        }

        LocalDateTime reservationExpiry = LocalDateTime.now().plusMinutes(durationMinutes);
        product.setReservedQuantity(quantity);
        product.setReservedUntil(reservationExpiry);

        Product updatedProduct = productRepository.save(product);
        log.info("Reservation completed successfully until: {}", reservationExpiry);

        return new StockResponse(
                updatedProduct.getId(),
                updatedProduct.getStock(),
                updatedProduct.getAvailableStock(),
                updatedProduct.getReservedQuantity()
        );
    }

    @Override
    @Transactional
    public int releaseExpiredReservations() {
        log.debug("Checking for expired reservations");

        var expiredProducts = productRepository.findProductsWithExpiredReservations(LocalDateTime.now());
        expiredProducts.forEach(product -> {
            log.info("Releasing expired reservation for product id: {}", product.getId());
            product.setReservedQuantity(null);
            product.setReservedUntil(null);
        });

        if (!expiredProducts.isEmpty()) {
            productRepository.saveAll(expiredProducts);
            log.info("Released {} expired reservations", expiredProducts.size());
        }

        return expiredProducts.size();
    }

    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}