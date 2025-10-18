package dev.amirgol.stockmanager.schedule;

import dev.amirgol.stockmanager.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to automatically release expired product reservations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ProductService productService;

    /**
     * Runs every minute to check and release expired reservations.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void releaseExpiredReservations() {
        log.trace("Running scheduled task: release expired reservations");
        int releasedCount = productService.releaseExpiredReservations();

        if (releasedCount > 0) {
            log.info("Scheduled task completed: released {} reservations", releasedCount);
        }
    }
}