package dev.amirgol.stockmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.model.Product;
import dev.amirgol.stockmanager.model.User;
import dev.amirgol.stockmanager.model.enums.Role;
import dev.amirgol.stockmanager.repository.ProductRepository;
import dev.amirgol.stockmanager.repository.UserRepository;
import dev.amirgol.stockmanager.security.jwt.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String managerToken;
    private String userToken;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        User manager = createUser("manager", Set.of(Role.MANAGER, Role.USER));
        User user = createUser("user", Set.of(Role.USER));

        managerToken = jwtService.generateToken(manager);
        userToken = jwtService.generateToken(user);

        Product product = Product.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .build();
        testProductId = productRepository.save(product).getId();
    }

    private User createUser(String username, Set<Role> roles) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password"))
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        return userRepository.save(user);
    }

    @Test
    void createProduct_Success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void createProduct_UserRole_Forbidden() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Product")
                .price(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProduct_Success() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProductId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProduct_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void buyProduct_Success() throws Exception {
        mockMvc.perform(post("/api/products/{id}/buy", testProductId)
                        .header("Authorization", "Bearer " + userToken)
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(90));
    }

    @Test
    void buyProduct_InsufficientStock() throws Exception {
        mockMvc.perform(post("/api/products/{id}/buy", testProductId)
                        .header("Authorization", "Bearer " + userToken)
                        .param("quantity", "200"))
                .andExpect(status().isBadRequest());
    }
}
