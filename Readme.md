# 🏪 Stock Manager API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-63%20Passing-success?style=for-the-badge)

**An e-commerce inventory management system with JWT authentication, real-time stock tracking, and automatic reservation management.**

[Features](#-features) • [Quick Start](#-quick-start) • [API Documentation](#-api-documentation) • [Architecture](#-architecture) • [Contributing](#-contributing) • [Roadmap](#️-roadmap)

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
- [Roadmap](#️-roadmap)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 🛍️ **Product Management** - Create, view, and manage product inventory.
- 📊 **Real-time Stock Tracking** - Monitor available and reserved stock levels.
- 🛒 **Purchase Operations** - Process product purchases with stock validation.
- 🔒 **Reservation System** - Reserve stock with automatic expiration.
- ♻️ **Auto-cleanup** - Scheduled task to release expired reservations.

### Technical Features
- 🔐 **JWT Authentication** - Secure token-based authentication.
- 👥 **Role-Based Access Control** - USER, MANAGER, and ADMIN roles.
- 📝 **OpenAPI/Swagger** - Interactive API documentation.
- 🧪 **Tests** - 63 tests with a 100% pass rate.
- 🐳 **Docker Support** - Containerized PostgreSQL database.
- 🔄 **Transaction Safety** - JPA transactions with optimistic locking.
- 📈 **Clean Architecture** - Multi-tier design following SOLID principles.
- 🎯 **MapStruct Integration** - Type-safe DTO mapping.

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
- 💻 **IDE** (IntelliJ IDEA, Eclipse, or VS Code)

**Verify Installation:**

java -version    # Should show Java 21+
mvn -version     # Should show Maven 3.8+
docker --version # Should show Docker 20+

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

---

## 📚 API Documentation

### Available Endpoints

#### Authentication APIs

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Authenticate a user | ❌ |

#### Product Management APIs

| Method | Endpoint | Description | Auth Required | Roles |
| --- | --- | --- | --- | --- |
| `POST` | `/api/products` | Create a product | ✅ | MANAGER, ADMIN |
| `GET` | `/api/products/{id}` | Get product details | ✅ | USER, MANAGER, ADMIN |
| `GET` | `/api/products/{id}/stock` | Get stock info | ✅ | USER, MANAGER, ADMIN |

#### Stock Operations APIs

| Method | Endpoint | Description | Auth Required | Roles |
| --- | --- | --- | --- | --- |
| `POST` | `/api/products/{id}/refill` | Refill stock | ✅ | MANAGER, ADMIN |
| `POST` | `/api/products/{id}/buy` | Purchase a product | ✅ | USER, MANAGER, ADMIN |
| `POST` | `/api/products/{id}/reserve` | Reserve stock | ✅ | USER, MANAGER, ADMIN |

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
| --- | --- | --- |
| **USER** | Standard customer | View products, buy items, reserve stock |
| **MANAGER** | Inventory manager | Everything USER can do + create products, refill stock |
| **ADMIN** | System administrator | Full system access, user management |

### Security Features

* ✅ Password encryption using BCrypt (strength 12)
* ✅ CSRF protection disabled for stateless API
* ✅ CORS configured for common frontend ports
* ✅ Stateless session management
* ✅ JWT token validation on every request
* ✅ Method-level security with `@PreAuthorize`
* ✅ Input validation using Bean Validation
* ✅ SQL injection protection via JPA/Hibernate

---

## 🏗️ Architecture

### Project Structure

```
StockManager/
├── src/
│   ├── main/
│   │   ├── java/dev/amirgol/stockmanager/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Exception handling
│   │   │   ├── model/               # JPA Entities
│   │   │   ├── repository/          # Data Access Layer
│   │   │   ├── security/            # Security components
│   │   │   ├── service/             # Business Logic
│   │   │   └── Application.java     # Main class
│   │   └── resources/               # Application configuration
│   └── test/                        # Unit and integration tests
├── docker-compose.yml               # PostgreSQL container
└── pom.xml                          # Maven dependencies

```

### Layer Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (REST Controllers, DTOs, Mappers)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Service Layer                  │
│    (Business Logic, Transactions)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        Repository Layer                 │
│    (Data Access, JPA Repositories)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Database Layer                 │
│         (PostgreSQL 16)                 │
└─────────────────────────────────────────┘

```

---

## 🧪 Testing

### Test Coverage

```
Total Tests: 63
✅ Passing: 63 (100%)
❌ Failing: 0
⏭️ Skipped: 0

```

### Running Tests

**Run All Tests:**

```bash
mvn test

```

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default | Required |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5332/stockdb` | No |
| `SPRING_DATASOURCE_USERNAME` | Database username | `stockuser` | No |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `stockpass` | No |
| `JWT_SECRET_KEY` | JWT signing key | Default key | **Yes** (Production) |
| `JWT_EXPIRATION` | Token expiration time in milliseconds | `86400000` (24h) | No |
| `SERVER_PORT` | Application port | `8080` | No |

---

## 🚢 Deployment

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

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** your changes (`mvn test`)
5. **Submit** a pull request

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.

---

## 👤 Author

**Amir Golmoradi**

* GitHub: [@Amir-Golmoradi](https://github.com/Amir-Golmoradi)
* Email: contact@amirgol.dev

---

## 🗺️ Roadmap

### 🏗️ Architecture & Security Changes

* [ ] **DDD & CQRS Refactoring** - Refactor the entire project based on Domain-Driven Design (DDD), Hexagonal Architecture, and CQRS.
* [ ] **Keycloak Security** - Change security to use OAuth2 and Keycloak.
* [ ] **Microservices Conversion** - Move the project from a monolith to a complete microservice architecture.

### 🧼 Code Quality Tools
* [ ] Spotless - Add automated code formatting.
* [ ] Checkstyle - Enforce code style rules.
* [ ] SpotBugs - Scan code to find bugs.
* [ ] PMD - Analyze code for bad practices.

### 🎯 Feature Additions

* [ ] Add pagination for product listings.
* [ ] Implement product search and filtering.
* [ ] Add email notifications for low stock.
* [ ] Create an admin dashboard.
* [ ] Add metrics and monitoring (Prometheus/Grafana).
* [ ] Implement a caching layer (Redis).
* [ ] Add GraphQL API support.
* [ ] Add multi-language support (i18n).
* [ ] Add product categories and tags.
* [ ] Implement an order management system.

---

### ⭐ Star this repo if you find it helpful!

Made by [Amir Golmoradi](https://github.com/Amir-Golmoradi)

[⬆ Back to Top](https://www.google.com/search?q=%23-stock-manager-api)
