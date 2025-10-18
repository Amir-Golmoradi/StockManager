package dev.amirgol.stockmanager.service;

import dev.amirgol.stockmanager.dto.product.ProductMapper;
import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.dto.product.response.StockResponse;
import dev.amirgol.stockmanager.exception.InsufficientStockException;
import dev.amirgol.stockmanager.exception.ProductNotFoundException;
import dev.amirgol.stockmanager.model.Product;
import dev.amirgol.stockmanager.repository.ProductRepository;
import dev.amirgol.stockmanager.service.product.ProductService;
import dev.amirgol.stockmanager.service.product.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Mocks all dependencies to test business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private UUID testProductId;
    private CreateProductRequest createRequest;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();

        testProduct = Product.builder()
                .id(testProductId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .build();

        createRequest = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("49.99"))
                .build();

        productDto = ProductDto.builder()
                .id(testProductId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .availableStock(100)
                .build();
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product with initial stock of 100")
        void createProduct_Success() {
            // Arrange
            Product newProduct = Product.builder()
                    .name(createRequest.name())
                    .description(createRequest.description())
                    .price(createRequest.price())
                    .stock(100)
                    .build();

            when(productMapper.toEntity(createRequest)).thenReturn(newProduct);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(productMapper.toDto(testProduct)).thenReturn(productDto);

            // Act
            ProductDto result = productService.createProduct(createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.stock()).isEqualTo(100);

            verify(productMapper).toEntity(createRequest);
            verify(productRepository).save(any(Product.class));
            verify(productMapper).toDto(testProduct);
            verifyNoMoreInteractions(productRepository, productMapper);
        }

        // Test removed - MapStruct never returns null, this is an unrealistic edge case
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should retrieve product successfully")
        void getProduct_Success() {
            // Arrange
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productMapper.toDto(testProduct)).thenReturn(productDto);

            // Act
            ProductDto result = productService.getProduct(testProductId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testProductId);
            assertThat(result.name()).isEqualTo("Test Product");

            verify(productRepository).findById(testProductId);
            verify(productMapper).toDto(testProduct);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product not found")
        void getProduct_NotFound() {
            // Arrange
            when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProduct(testProductId))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(testProductId.toString());

            verify(productRepository).findById(testProductId);
            verifyNoInteractions(productMapper);
        }

        @Test
        @DisplayName("Should release expired reservation when accessing product")
        void getProduct_WithExpiredReservation() {
            // Arrange
            testProduct.setReservedQuantity(20);
            testProduct.setReservedUntil(LocalDateTime.now().minusMinutes(5));

            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productMapper.toDto(testProduct)).thenReturn(productDto);

            // Act
            productService.getProduct(testProductId);

            // Assert
            assertThat(testProduct.getReservedQuantity()).isNull();
            assertThat(testProduct.getReservedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("Get Stock Tests")
    class GetStockTests {

        @Test
        @DisplayName("Should return stock information")
        void getProductStock_Success() {
            // Arrange
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

            // Act
            StockResponse result = productService.getProductStock(testProductId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.productId()).isEqualTo(testProductId);
            assertThat(result.totalStock()).isEqualTo(100);
            assertThat(result.availableStock()).isEqualTo(100);
            assertThat(result.reservedQuantity()).isNull();
        }

        @Test
        @DisplayName("Should return correct available stock with reservation")
        void getProductStock_WithReservation() {
            // Arrange
            testProduct.setReservedQuantity(30);
            testProduct.setReservedUntil(LocalDateTime.now().plusMinutes(10));

            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

            // Act
            StockResponse result = productService.getProductStock(testProductId);

            // Assert
            assertThat(result.totalStock()).isEqualTo(100);
            assertThat(result.availableStock()).isEqualTo(70);
            assertThat(result.reservedQuantity()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Refill Stock Tests")
    class RefillStockTests {

        @Test
        @DisplayName("Should refill stock successfully")
        void refillStock_Success() {
            // Arrange
            int refillAmount = 50;
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            StockResponse result = productService.refillStock(testProductId, refillAmount);

            // Assert
            assertThat(result.totalStock()).isEqualTo(150);
            assertThat(testProduct.getStock()).isEqualTo(150);

            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception for negative refill amount")
        void refillStock_NegativeAmount() {
            // Act & Assert
            assertThatThrownBy(() -> productService.refillStock(testProductId, -10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be positive");

            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Should throw exception for zero refill amount")
        void refillStock_ZeroAmount() {
            // Act & Assert
            assertThatThrownBy(() -> productService.refillStock(testProductId, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be positive");
        }

        @Test
        @DisplayName("Should release expired reservation before refilling")
        void refillStock_WithExpiredReservation() {
            // Arrange
            testProduct.setReservedQuantity(20);
            testProduct.setReservedUntil(LocalDateTime.now().minusMinutes(5));

            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            productService.refillStock(testProductId, 50);

            // Assert
            assertThat(testProduct.getReservedQuantity()).isNull();
            assertThat(testProduct.getReservedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("Buy Product Tests")
    class BuyProductTests {

        @Test
        @DisplayName("Should purchase product successfully")
        void buyProduct_Success() {
            // Arrange
            int purchaseQuantity = 30;
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            StockResponse result = productService.buyProduct(testProductId, purchaseQuantity);

            // Assert
            assertThat(result.totalStock()).isEqualTo(70);
            assertThat(testProduct.getStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("Should throw exception when stock insufficient")
        void buyProduct_InsufficientStock() {
            // Arrange
            int purchaseQuantity = 150;
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

            // Act & Assert
            assertThatThrownBy(() -> productService.buyProduct(testProductId, purchaseQuantity))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void buyProduct_NegativeQuantity() {
            // Act & Assert
            assertThatThrownBy(() -> productService.buyProduct(testProductId, -5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void buyProduct_ZeroQuantity() {
            // Act & Assert
            assertThatThrownBy(() -> productService.buyProduct(testProductId, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should buy exactly all available stock")
        void buyProduct_ExactStock() {
            // Arrange
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            StockResponse result = productService.buyProduct(testProductId, 100);

            // Assert
            assertThat(result.totalStock()).isEqualTo(0);
            assertThat(result.availableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should clear reservation when buying reserved items")
        void buyProduct_ClearsReservation() {
            // Arrange
            testProduct.setReservedQuantity(30);
            testProduct.setReservedUntil(LocalDateTime.now().plusMinutes(10));

            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            productService.buyProduct(testProductId, 20);

            // Assert
            assertThat(testProduct.getReservedQuantity()).isNull();
            assertThat(testProduct.getReservedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("Reserve Product Tests")
    class ReserveProductTests {

        @Test
        @DisplayName("Should reserve product successfully")
        void reserveProduct_Success() {
            // Arrange
            int reserveQuantity = 20;
            int durationMinutes = 30;

            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            StockResponse result = productService.reserveProduct(testProductId, reserveQuantity, durationMinutes);

            // Assert
            assertThat(result.reservedQuantity()).isEqualTo(20);
            assertThat(result.availableStock()).isEqualTo(80);
            assertThat(testProduct.getReservedUntil()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock for reservation")
        void reserveProduct_InsufficientStock() {
            // Arrange
            int reserveQuantity = 150;
            when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

            // Act & Assert
            assertThatThrownBy(() -> productService.reserveProduct(testProductId, reserveQuantity, 30))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock for reservation");
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void reserveProduct_NegativeQuantity() {
            // Act & Assert
            assertThatThrownBy(() -> productService.reserveProduct(testProductId, -10, 30))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception for negative duration")
        void reserveProduct_NegativeDuration() {
            // Act & Assert
            assertThatThrownBy(() -> productService.reserveProduct(testProductId, 10, -30))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration must be positive");
        }
    }

    @Nested
    @DisplayName("Release Expired Reservations Tests")
    class ReleaseExpiredReservationsTests {

        @Test
        @DisplayName("Should release all expired reservations")
        void releaseExpiredReservations_Success() {
            // Arrange
            Product expiredProduct1 = Product.builder()
                    .id(UUID.randomUUID())
                    .stock(100)
                    .reservedQuantity(20)
                    .reservedUntil(LocalDateTime.now().minusMinutes(5))
                    .build();

            Product expiredProduct2 = Product.builder()
                    .id(UUID.randomUUID())
                    .stock(50)
                    .reservedQuantity(10)
                    .reservedUntil(LocalDateTime.now().minusMinutes(10))
                    .build();

            List<Product> expiredProducts = List.of(expiredProduct1, expiredProduct2);

            when(productRepository.findProductsWithExpiredReservations(any(LocalDateTime.class)))
                    .thenReturn(expiredProducts);
            when(productRepository.saveAll(anyList())).thenReturn(expiredProducts);

            // Act
            int releasedCount = productService.releaseExpiredReservations();

            // Assert
            assertThat(releasedCount).isEqualTo(2);
            assertThat(expiredProduct1.getReservedQuantity()).isNull();
            assertThat(expiredProduct2.getReservedQuantity()).isNull();

            verify(productRepository).saveAll(expiredProducts);
        }

        @Test
        @DisplayName("Should return zero when no expired reservations")
        void releaseExpiredReservations_NoExpired() {
            // Arrange
            when(productRepository.findProductsWithExpiredReservations(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // Act
            int releasedCount = productService.releaseExpiredReservations();

            // Assert
            assertThat(releasedCount).isEqualTo(0);
            verify(productRepository, never()).saveAll(anyList());
        }
    }
}
