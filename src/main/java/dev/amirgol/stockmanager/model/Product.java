package dev.amirgol.stockmanager.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product entity representing an item in the inventory.
 * Thread-safety: JPA entities are not thread-safe.
 * Concurrent access is managed through transaction isolation.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    /**
     * Timestamp until which the stock is reserved.
     * Null means no active reservation.
     */
    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculates available stock (total stock minus reserved quantity).
     *
     * @return available stock count
     */
    public int getAvailableStock() {
        if (reservedQuantity == null || reservedUntil == null || reservedUntil.isBefore(LocalDateTime.now())) {
            return stock;
        }
        return stock - reservedQuantity;
    }

    /**
     * Checks if reservation has expired.
     *
     * @return true if reservation is expired or no reservation exists
     */
    public boolean isReservationExpired() {
        return reservedUntil == null || reservedUntil.isBefore(LocalDateTime.now());
    }

    /**
     * Releases expired reservation by clearing reservation fields.
     */
    public void releaseExpiredReservation() {
        if (isReservationExpired() && reservedQuantity != null) {
            this.reservedQuantity = null;
            this.reservedUntil = null;
        }
    }
}