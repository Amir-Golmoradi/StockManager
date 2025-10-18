package dev.amirgol.stockmanager.model.enums;


/**
 * User roles for authorization.
 */
public enum Role {
    USER,       // Basic user - can view products
    MANAGER,    // Can manage stock (refill, buy)
    ADMIN       // Full access including user management
}