# Stock Manager REST API

A production-grade Spring Boot application for managing e-commerce product inventory with support for stock operations, reservations, and automated cleanup.

## 🎯 Features

- **Product Management**: Create and retrieve products
- **Stock Operations**: Refill, buy, and check stock levels
- **Reservation System**: Temporarily reserve stock with automatic expiration
- **Transaction Safety**: Proper JPA transactions with optimistic locking
- **Clean Architecture**: Multi-tier design following SOLID principles
- **Full Documentation**: OpenAPI/Swagger UI integration
- **Comprehensive Testing**: Unit tests with 80%+ coverage
- **Docker Support**: Containerized PostgreSQL setup

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **PostgreSQL 16**
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **JUnit 5 + Mockito** for testing
- **OpenAPI 3** for API documentation
- **Docker Compose** for local development

## 📋 Prerequisites

- JDK 21 or higher
- Maven 3.8+
- Docker and Docker Compose
- (Optional) Your favorite IDE with Lombok plugin

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Amir-Golmoradi/StockManager.git
cd StockManager


## 🔐 Authentication & Authorization

### Authentication Flow

1. **Register** a new user or use default credentials
2. **Login** to receive a JWT token
3. **Include token** in subsequent requests: `Authorization: Bearer <token>`

### Default Users

After starting the application, the following users are available:

| Username | Password   | Roles                |
|----------|------------|----------------------|
| admin    | admin123   | ADMIN, MANAGER, USER |
| manager  | manager123 | MANAGER, USER        |
| user     | user123    | USER                 |

### Role-Based Access Control

| Endpoint | USER | MANAGER | ADMIN |
|----------|------|---------|-------|
| GET /api/products/* | ✅ | ✅ | ✅ |
| POST /api/products | ❌ | ✅ | ✅ |
| POST /api/products/*/refill | ❌ | ✅ | ✅ |
| POST /api/products/*/buy | ✅ | ✅ | ✅ |
| POST /api/products/*/reserve | ✅ | ✅ | ✅ |

### API Examples with Authentication

#### 1. Register New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepass123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"],
  "expiresIn": 86400000
}
```

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 3. Access Protected Endpoint

```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Create product (requires MANAGER or ADMIN role)
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99
  }'

# Get product (requires any authenticated user)
curl http://localhost:8080/api/products/{product-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Testing in Swagger UI

1. Navigate to http://localhost:8080/swagger-ui.html
2. Click "Authorize" button (🔓 icon)
3. Enter: `Bearer <your-jwt-token>`
4. Click "Authorize" and "Close"
5. All requests will now include the token

### CORS Configuration

The application is configured to allow requests from:
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)
- `http://localhost:8081` (Alternative frontend)

To add more origins, update `SecurityConfig.corsConfigurationSource()`.

### JWT Configuration

JWT tokens are configured with:
- **Algorithm**: HS256
- **Expiration**: 24 hours (configurable via `JWT_EXPIRATION`)
- **Claims**: username, roles, issued-at, expiration

To generate a secure secret key:
```bash
openssl rand -base64 64
```

Update `JWT_SECRET_KEY` in `.env` file.

### Security Best Practices

1. **Never commit** `.env` with real credentials
2. **Use strong passwords** in production
3. **Rotate JWT secrets** periodically
4. **Set appropriate token expiration** based on your needs
5. **Use HTTPS** in production
6. **Implement refresh tokens** for long-lived sessions (optional enhancement)

### Method-Level Security

The application uses `@PreAuthorize` annotations for fine-grained access control:

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }

@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public void managerOrAdminMethod() { ... }

@PreAuthorize("hasRole('USER') and #username == authentication.name")
public void userOwnsResource(String username) { ... }
```

## 🧪 Testing with Security

### Unit Tests with Security Context

```java
@Test
@WithMockUser(roles = "MANAGER")
void testWithManagerRole() {
    // Test code with MANAGER role
}

@Test
@WithMockUser(username = "admin", roles = {"ADMIN", "MANAGER"})
void testWithAdminRole() {
    // Test code with ADMIN role
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void accessProtectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void accessProtectedEndpoint_WithToken_Returns200() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk());
    }
}
```