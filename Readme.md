# 🏪 Stock Manager API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-63%20Passing-success?style=for-the-badge)

**A production-grade e-commerce inventory management system with JWT authentication, real-time stock tracking, and automated reservation management.**

[Features](#-features) • [Quick Start](#-quick-start) • [API Documentation](#-api-documentation) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#️-technology-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Authentication & Security](#-authentication--security)
- [Architecture](#-architecture)
- [Testing](#-testing)
- [Configuration](#️-configuration)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 🛍️ **Product Management** - Create, retrieve, and manage product inventory
- 📊 **Real-time Stock Tracking** - Monitor available and reserved stock levels
- 🛒 **Purchase Operations** - Process product purchases with stock validation
- 🔒 **Reservation System** - Temporarily reserve stock with automatic expiration
- ♻️ **Auto-cleanup** - Scheduled task to release expired reservations

### Technical Features
- 🔐 **JWT Authentication** - Secure token-based authentication
- 👥 **Role-Based Access Control** - USER, MANAGER, and ADMIN roles
- 📝 **OpenAPI/Swagger** - Interactive API documentation
- 🧪 **Comprehensive Testing** - 63 tests with 100% pass rate
- 🐳 **Docker Support** - Containerized PostgreSQL database
- 🔄 **Transaction Safety** - JPA transactions with optimistic locking
- 📈 **Clean Architecture** - Multi-tier design following SOLID principles
- 🎯 **MapStruct Integration** - Type-safe DTO mapping

---

## 🛠️ Technology Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.6 |
| **Security** | Spring Security 6, JWT (JJWT 0.12.3) |
| **Database** | PostgreSQL 16, Spring Data JPA, Hibernate |
| **Testing** | JUnit 5, Mockito, H2 (in-memory) |
| **Documentation** | SpringDoc OpenAPI 3 |
| **Mapping** | MapStruct 1.6.3 |
| **Build Tool** | Maven 3.8+ |
| **Utilities** | Lombok, Jackson |

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java 21** or higher ([Download](https://adoptium.net/))
- 🔨 **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- 🐳 **Docker & Docker Compose** ([Download](https://www.docker.com/get-started))
- 💻 **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

**Verify Installation:**
```bash
java -version    # Should show Java 21+
mvn -version     # Should show Maven 3.8+
docker --version # Should show Docker 20+
```

---

## 🚀 Quick Start

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Amir-Golmoradi/StockManager.git
cd StockManager
```

### 2️⃣ Start PostgreSQL Database

```bash
# Create environment file
cat > .env << 'EOF'
POSTGRES_DB=stockdb
POSTGRES_USER=stockuser
POSTGRES_PASSWORD=stockpass
EOF

# Start PostgreSQL with Docker Compose
docker-compose up -d

# Verify database is running
docker-compose ps
```

### 3️⃣ Run the Application

**Option A: Using Maven (Development)**
```bash
mvn spring-boot:run
```

**Option B: Using the Restart Script**
```bash
chmod +x restart-app.sh
./restart-app.sh
```

**Option C: Build and Run JAR**
```bash
mvn clean package -DskipTests
java -jar target/StockManager-0.0.1-SNAPSHOT.jar
```

### 4️⃣ Verify Application is Running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

### 5️⃣ Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui/index.html
```

🎉 **You're ready to go!** Check out the [API Documentation](#-api-documentation) section to start testing.

---

## 📚 API Documentation

### Available Endpoints

#### Authentication APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register new user | ❌ |
| `POST` | `/api/auth/login` | Authenticate user | ❌ |

#### Product Management APIs
| Method | Endpoint | Description | Auth Required | Roles |
|--------|----------|-------------|---------------|-------|
| `POST` | `/api/products` | Create product | ✅ | MANAGER, ADMIN |
| `GET` | `/api/products/{id}` | Get product details | ✅ | USER, MANAGER, ADMIN |
| `GET` | `/api/products/{id}/stock` | Get stock info | ✅ | USER, MANAGER, ADMIN |

#### Stock Operations APIs
| Method | Endpoint | Description | Auth Required | Roles |
|--------|----------|-------------|---------------|-------|
| `POST` | `/api/products/{id}/refill` | Refill stock | ✅ | MANAGER, ADMIN |
| `POST` | `/api/products/{id}/buy` | Purchase product | ✅ | USER, MANAGER, ADMIN |
| `POST` | `/api/products/{id}/reserve` | Reserve stock | ✅ | USER, MANAGER, ADMIN |

### Interactive API Testing

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

**API Documentation (JSON):**
```
http://localhost:8080/v3/api-docs
```

### Example API Calls

#### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "roles": ["ROLE_USER"]
}
```

#### 2. Create a Product (MANAGER/ADMIN)
```bash
TOKEN="your_jwt_token_here"

curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stock": 100,
  "availableStock": 100
}
```

#### 3. Buy a Product (USER)
```bash
curl -X POST "http://localhost:8080/api/products/{id}/buy?quantity=5" \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "totalStock": 95,
  "availableStock": 95,
  "reservedQuantity": null
}
```

#### 4. Reserve Stock (USER)
```bash
curl -X POST "http://localhost:8080/api/products/{id}/reserve?quantity=10&duration=30" \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "totalStock": 95,
  "availableStock": 85,
  "reservedQuantity": 10,
  "reservedUntil": "2025-10-18T19:30:00"
}
```

---

## 🔐 Authentication & Security

### JWT Authentication Flow

```
┌─────────┐           ┌─────────┐           ┌──────────┐
│ Client  │           │   API   │           │ Database │
└────┬────┘           └────┬────┘           └────┬─────┘
     │                     │                     │
     │  POST /auth/login   │                     │
     │────────────────────>│                     │
     │                     │  Validate User      │
     │                     │────────────────────>│
     │                     │<────────────────────│
     │                     │  Generate JWT       │
     │   JWT Token         │                     │
     │<────────────────────│                     │
     │                     │                     │
     │  GET /api/products  │                     │
     │  + Bearer Token     │                     │
     │────────────────────>│                     │
     │                     │  Validate Token     │
     │                     │  Extract User       │
     │                     │  Check Permissions  │
     │                     │  Query Data         │
     │                     │────────────────────>│
     │   Response          │<────────────────────│
     │<────────────────────│                     │
```

### Role-Based Access Control

| Role | Description | Capabilities |
|------|-------------|--------------|
| **USER** | Standard customer | View products, buy items, reserve stock |
| **MANAGER** | Inventory manager | Everything USER can do + create products, refill stock |
| **ADMIN** | System administrator | Full system access, user management |

### Using JWT Tokens in Swagger

1. **Get Your Token:**
   - Register via `POST /api/auth/register`
   - Or login via `POST /api/auth/login`
   - Copy the `token` value from response

2. **Authorize in Swagger:**
   - Click the **"Authorize"** button (🔓) at the top of Swagger UI
   - Enter: `Bearer YOUR_TOKEN_HERE`
   - Click **"Authorize"** then **"Close"**

3. **Test Endpoints:**
   - All subsequent requests will include your JWT token
   - Lock icon changes to 🔒 showing you're authenticated

### Token Configuration

- **Algorithm:** HS512
- **Default Expiration:** 24 hours
- **Custom Expiration:** Set `JWT_EXPIRATION` environment variable (milliseconds)
- **Secret Key:** Configure via `JWT_SECRET_KEY` environment variable

**Generate Secure Secret:**
```bash
# Generate a secure 512-bit secret
openssl rand -base64 64
```

### Security Features

- ✅ Password encryption using BCrypt (strength 12)
- ✅ CSRF protection disabled for stateless API
- ✅ CORS configured for common frontend ports
- ✅ Stateless session management
- ✅ JWT token validation on every request
- ✅ Method-level security with `@PreAuthorize`
- ✅ Input validation using Bean Validation
- ✅ SQL injection protection via JPA/Hibernate

---

## 🏗️ Architecture

### Project Structure

```
StockManager/
├── src/
│   ├── main/
│   │   ├── java/dev/amirgol/stockmanager/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── AuthRestController.java
│   │   │   │   └── ProductRestController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── auth/
│   │   │   │   └── product/
│   │   │   ├── exception/           # Exception handling
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── Custom exceptions...
│   │   │   ├── model/               # JPA Entities
│   │   │   │   ├── Product.java
│   │   │   │   └── User.java
│   │   │   ├── repository/          # Data Access Layer
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/            # Security components
│   │   │   │   ├── jwt/
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── auth/
│   │   │   │   └── product/
│   │   │   └── Application.java     # Main class
│   │   └── resources/
│   │       ├── application.yml      # Main configuration
│   │       └── SQL/                 # Database scripts
│   └── test/
│       ├── java/                    # Test classes
│       │   ├── controller/          # Controller tests
│       │   ├── service/             # Service tests
│       │   ├── repository/          # Repository tests
│       │   └── integration/         # Integration tests
│       └── resources/
│           └── application-test.yml # Test configuration
├── docker-compose.yml               # PostgreSQL container
├── pom.xml                          # Maven dependencies
├── restart-app.sh                   # Restart automation script
├── SWAGGER_GUIDE.md                 # Swagger troubleshooting
├── QUICK_START.md                   # Quick reference guide
└── README.md                        # You are here!
```

### Layer Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (REST Controllers, DTOs, Mappers)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Service Layer                  │
│   (Business Logic, Transactions)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Repository Layer                  │
│   (Data Access, JPA Repositories)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Database Layer                 │
│        (PostgreSQL 16)                  │
└─────────────────────────────────────────┘
```

### Design Patterns Used

- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Data transfer between layers
- **Builder Pattern** - Object construction (via Lombok)
- **Dependency Injection** - Loose coupling via Spring
- **Strategy Pattern** - Authentication strategies
- **Template Method** - Spring's JpaRepository
- **Singleton Pattern** - Spring beans
- **Factory Pattern** - EntityManager, JWT generation

### SOLID Principles

✅ **Single Responsibility** - Each class has one reason to change  
✅ **Open/Closed** - Open for extension, closed for modification  
✅ **Liskov Substitution** - Interfaces used throughout  
✅ **Interface Segregation** - Specific, focused interfaces  
✅ **Dependency Inversion** - Depend on abstractions, not concretions  

---

## 🧪 Testing

### Test Coverage

```
Total Tests: 63
✅ Passing: 63 (100%)
❌ Failing: 0
⏭️  Skipped: 0
```

### Test Categories

| Category | Tests | Description |
|----------|-------|-------------|
| **Unit Tests** | 43 | Controller, Service, Repository tests |
| **Integration Tests** | 11 | End-to-end API tests |
| **Component Tests** | 9 | Repository layer tests |

### Running Tests

**Run All Tests:**
```bash
mvn test
```

**Run Specific Test Class:**
```bash
mvn test -Dtest=ProductServiceTest
```

**Run with Coverage:**
```bash
mvn clean test jacoco:report
```

**Skip Tests During Build:**
```bash
mvn clean package -DskipTests
```

### Test Structure

```java
// Example: Service Layer Test
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository repository;
    
    @Mock
    private ProductMapper mapper;
    
    @InjectMocks
    private ProductServiceImpl service;
    
    @Test
    void createProduct_Success() {
        // Arrange
        CreateProductRequest request = ...;
        when(mapper.toEntity(request)).thenReturn(product);
        when(repository.save(any())).thenReturn(savedProduct);
        
        // Act
        ProductDto result = service.createProduct(request);
        
        // Assert
        assertThat(result).isNotNull();
        verify(repository).save(any());
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void createProduct_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }
}
```

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5332/stockdb` | No |
| `SPRING_DATASOURCE_USERNAME` | Database username | `stockuser` | No |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `stockpass` | No |
| `JWT_SECRET_KEY` | JWT signing secret (base64) | Default key | **Yes** (Production) |
| `JWT_EXPIRATION` | Token expiration (milliseconds) | `86400000` (24h) | No |
| `SERVER_PORT` | Application port | `8080` | No |

### Configuration Files

**Main Configuration (`application.yml`):**
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5332/stockdb}
    username: ${SPRING_DATASOURCE_USERNAME:stockuser}
    password: ${SPRING_DATASOURCE_PASSWORD:stockpass}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

security:
  jwt:
    secret-key: ${JWT_SECRET_KEY:your-secret-key}
    expiration-time: ${JWT_EXPIRATION:86400000}

server:
  port: ${SERVER_PORT:8080}
```

**Test Configuration (`application-test.yml`):**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Docker Compose Configuration

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    container_name: stock-manager-db
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5332:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 🚢 Deployment

### Building for Production

```bash
# Build optimized JAR
mvn clean package -DskipTests

# JAR location
ls -lh target/StockManager-0.0.1-SNAPSHOT.jar
```

### Running in Production

```bash
# Set environment variables
export JWT_SECRET_KEY="your-secure-base64-secret"
export SPRING_DATASOURCE_URL="jdbc:postgresql://prod-db:5432/stockdb"
export SPRING_DATASOURCE_USERNAME="produser"
export SPRING_DATASOURCE_PASSWORD="prodpass"

# Run application
java -jar target/StockManager-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

**Create Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/StockManager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and Run:**
```bash
# Build image
docker build -t stock-manager:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e JWT_SECRET_KEY="your-secret" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host:5432/db" \
  --name stock-manager \
  stock-manager:latest
```

### Health Checks

Monitor application health:
```bash
curl http://localhost:8080/actuator/health
```

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Getting Started

1. **Fork** the repository
2. **Clone** your fork
3. **Create** a feature branch
4. **Make** your changes
5. **Test** thoroughly
6. **Submit** a pull request

### Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build/tooling changes

**Example:**
```bash
git commit -m "feat(product): add bulk import functionality

- Add CSV parser for product import
- Implement validation for bulk data
- Add integration tests

Closes #123"
```

### Pull Request Process

1. Update documentation if needed
2. Add/update tests for new features
3. Ensure all tests pass: `mvn test`
4. Update CHANGELOG.md
5. Request review from maintainers

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok annotations to reduce boilerplate
- Write meaningful commit messages
- Add JavaDoc for public APIs
- Keep methods small and focused

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Amir Golmoradi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 👤 Author

**Amir Golmoradi**

- GitHub: [@Amir-Golmoradi](https://github.com/Amir-Golmoradi)
- Email: contact@amirgol.dev

---

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- PostgreSQL community for the robust database
- All contributors and testers

---

## 📞 Support

- 📖 [Documentation](SWAGGER_GUIDE.md)
- 🚀 [Quick Start Guide](QUICK_START.md)
- 🐛 [Issue Tracker](https://github.com/Amir-Golmoradi/StockManager/issues)
- 💬 [Discussions](https://github.com/Amir-Golmoradi/StockManager/discussions)

---

## 🗺️ Roadmap

- [ ] Add pagination for product listings
- [ ] Implement product search and filtering
- [ ] Add email notifications for low stock
- [ ] Create admin dashboard
- [ ] Add metrics and monitoring (Prometheus/Grafana)
- [ ] Implement caching layer (Redis)
- [ ] Add GraphQL API support
- [ ] Multi-language support (i18n)
- [ ] Add product categories and tags
- [ ] Implement order management system

---

<div align="center">

### ⭐ Star this repo if you find it helpful!

Made with ❤️ by [Amir Golmoradi](https://github.com/Amir-Golmoradi)

[⬆ Back to Top](#-stock-manager-api)

</div>