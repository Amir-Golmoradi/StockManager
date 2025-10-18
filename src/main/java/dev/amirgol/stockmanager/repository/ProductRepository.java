package dev.amirgol.stockmanager.repository;

import dev.amirgol.stockmanager.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Product entity.
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Finds all products with expired reservations.
     *
     * @param now current timestamp
     * @return list of products with expired reservations
     */
    @Query("SELECT p FROM Product p WHERE p.reservedUntil IS NOT NULL AND p.reservedUntil < :now")
    List<Product> findProductsWithExpiredReservations(LocalDateTime now);
}