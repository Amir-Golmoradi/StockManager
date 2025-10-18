package dev.amirgol.stockmanager.dto.auth.response;

import lombok.Builder;

import java.util.Set;

/**
 * Response DTO containing JWT token and user information.
 */
@Builder
public record AuthenticationResponse(
        String token,
        String type,
        String username,
        String email,
        Set<String> roles,
        Long expiresIn // milliseconds
) {
    // Default `type` to "Bearer" if null
    public AuthenticationResponse {
        if (type == null) {
            type = "Bearer";
        }
    }

    // Convenience constructor without type
    public AuthenticationResponse(String token, String username, String email, Set<String> roles, Long expiresIn) {
        this(token, "Bearer", username, email, roles, expiresIn);
    }
}
