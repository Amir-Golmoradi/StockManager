package dev.amirgol.stockmanager.service.auth;

import dev.amirgol.stockmanager.dto.auth.request.AuthenticationRequest;
import dev.amirgol.stockmanager.dto.auth.request.RegisterRequest;
import dev.amirgol.stockmanager.dto.auth.response.AuthenticationResponse;
import dev.amirgol.stockmanager.model.User;
import dev.amirgol.stockmanager.model.enums.Role;
import dev.amirgol.stockmanager.repository.UserRepository;
import dev.amirgol.stockmanager.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling user authentication and registration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with USER role.
     *
     * @param request registration details
     * @return authentication response with JWT token
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        String jwtToken = jwtService.generateToken(user);

        return buildAuthenticationResponse(savedUser, jwtToken);
    }

    /**
     * Authenticates user and generates JWT token.
     *
     * @param request authentication credentials
     * @return authentication response with JWT token
     */
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Attempting to authenticate user: {}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String jwtToken = jwtService.generateToken(user);
        log.info("User authenticated successfully: {}", user.getUsername());

        return buildAuthenticationResponse(user, jwtToken);
    }

    /**
     * Builds authentication response with user details and token.
     *
     * @param user authenticated user
     * @param token JWT token
     * @return authentication response
     */
    private AuthenticationResponse buildAuthenticationResponse(User user, String token) {
        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}