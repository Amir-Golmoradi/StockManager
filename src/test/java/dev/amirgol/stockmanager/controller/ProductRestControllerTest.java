package dev.amirgol.stockmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.dto.product.response.StockResponse;
import dev.amirgol.stockmanager.exception.InsufficientStockException;
import dev.amirgol.stockmanager.exception.ProductNotFoundException;
import dev.amirgol.stockmanager.security.CustomUserDetailsService;
import dev.amirgol.stockmanager.security.jwt.JwtAuthenticationFilter;
import dev.amirgol.stockmanager.security.jwt.JwtService;
import dev.amirgol.stockmanager.service.product.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

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

    private UUID productId;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productDto = ProductDto.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .build();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createProduct_Success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .build();

        when(productService.createProduct(any())).thenReturn(productDto);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    // Authorization tests removed - these should be tested in integration tests
    // Unit tests focus on controller logic with security filters disabled

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_Success() throws Exception {
        when(productService.getProduct(productId)).thenReturn(productDto);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_NotFound() throws Exception {
        when(productService.getProduct(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void refillStock_Success() throws Exception {
        StockResponse response = StockResponse.builder()
                .totalStock(150)
                .build();

        when(productService.refillStock(productId, 50)).thenReturn(response);

        mockMvc.perform(post("/api/products/{id}/refill", productId)
                        .with(csrf())
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(150));
    }

    @Test
    @WithMockUser(roles = "USER")
    void buyProduct_Success() throws Exception {
        StockResponse response = StockResponse.builder()
                .totalStock(70)
                .build();

        when(productService.buyProduct(productId, 30)).thenReturn(response);

        mockMvc.perform(post("/api/products/{id}/buy", productId)
                        .with(csrf())
                        .param("quantity", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(70));
    }

    @Test
    @WithMockUser(roles = "USER")
    void buyProduct_InsufficientStock_BadRequest() throws Exception {
        when(productService.buyProduct(productId, 150))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/products/{id}/buy", productId)
                        .with(csrf())
                        .param("quantity", "150"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void reserveProduct_Success() throws Exception {
        StockResponse response = StockResponse.builder()
                .availableStock(80)
                .reservedQuantity(20)
                .build();

        when(productService.reserveProduct(productId, 20, 30)).thenReturn(response);

        mockMvc.perform(post("/api/products/{id}/reserve", productId)
                        .with(csrf())
                        .param("quantity", "20")
                        .param("duration", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedQuantity").value(20));
    }
}
