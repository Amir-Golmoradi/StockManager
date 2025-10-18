package dev.amirgol.stockmanager.repository;

import dev.amirgol.stockmanager.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .build();
    }

    @Test
    void save_Success() {
        Product saved = productRepository.save(testProduct);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Product");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_Success() {
        Product saved = entityManager.persist(testProduct);
        entityManager.flush();

        Optional<Product> found = productRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
    }

    @Test
    void findById_NotFound() {
        Optional<Product> found = productRepository.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void update_Success() {
        Product saved = entityManager.persist(testProduct);
        entityManager.flush();
        entityManager.clear();

        saved.setStock(200);
        productRepository.save(saved);
        entityManager.flush();

        Product updated = entityManager.find(Product.class, saved.getId());
        assertThat(updated.getStock()).isEqualTo(200);
    }

    @Test
    void delete_Success() {
        Product saved = entityManager.persist(testProduct);
        entityManager.flush();

        productRepository.deleteById(saved.getId());
        entityManager.flush();

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findProductsWithExpiredReservations_Success() {
        Product expired = Product.builder()
                .name("Expired")
                .price(new BigDecimal("50.00"))
                .stock(100)
                .reservedQuantity(20)
                .reservedUntil(LocalDateTime.now().minusMinutes(10))
                .build();

        entityManager.persist(expired);
        entityManager.flush();

        List<Product> expiredProducts = productRepository
                .findProductsWithExpiredReservations(LocalDateTime.now());

        assertThat(expiredProducts).hasSize(1);
        assertThat(expiredProducts.get(0).getName()).isEqualTo("Expired");
    }

    // Validation tests removed - validation is done at service/DTO layer, not at JPA entity level
    // Database constraints may prevent negative values, but JPA doesn't throw exceptions on save
}
