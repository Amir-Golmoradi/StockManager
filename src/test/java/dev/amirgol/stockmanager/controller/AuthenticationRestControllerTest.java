package dev.amirgol.stockmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.amirgol.stockmanager.dto.auth.request.AuthenticationRequest;
import dev.amirgol.stockmanager.dto.auth.request.RegisterRequest;
import dev.amirgol.stockmanager.dto.auth.response.AuthenticationResponse;
import dev.amirgol.stockmanager.security.CustomUserDetailsService;
import dev.amirgol.stockmanager.security.jwt.JwtAuthenticationFilter;
import dev.amirgol.stockmanager.security.jwt.JwtService;
import dev.amirgol.stockmanager.service.auth.AuthenticationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthenticationResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .username("testuser")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();

        when(authenticationService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authenticationService).register(any());
    }

    @Test
    void register_InvalidData_BadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("ab")
                .email("invalid")
                .password("12")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_Success() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password");

        when(authenticationService.authenticate(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authenticationService).authenticate(any());
    }

    @Test
    void login_EmptyCredentials_BadRequest() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
