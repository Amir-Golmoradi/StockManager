package dev.amirgol.stockmanager.dto.auth.request;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for user information.
 */
public record UserDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean enabled,
        LocalDateTime createdAt
) {}
