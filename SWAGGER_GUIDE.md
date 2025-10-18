# 🚀 Swagger UI Access Guide - Stock Manager API

## 📋 Table of Contents
- [Quick Start](#quick-start)
- [Troubleshooting Swagger Access](#troubleshooting-swagger-access)
- [Available Swagger URLs](#available-swagger-urls)
- [Complete API Testing Workflow](#complete-api-testing-workflow)
- [Common Issues and Solutions](#common-issues-and-solutions)

---

## 🎯 Quick Start

### Step 1: Ensure Application is Running

```bash
# Check if the application is running
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Step 2: Access Swagger UI

Open your browser and navigate to **ANY** of these URLs:

✅ **Primary URL:**
```
http://localhost:8080/swagger-ui/index.html
```

✅ **Alternative URLs:**
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/swagger-ui/
```

✅ **API Docs (JSON format):**
```
http://localhost:8080/v3/api-docs
```

---

## 🔧 Troubleshooting Swagger Access

### Issue 1: "Cannot Connect" or "Timeout"

**Symptoms:** Browser shows "This site can't be reached" or keeps loading forever.

**Solutions:**

1. **Verify Application is Running:**
   ```bash
   # Check if port 8080 is listening
   netstat -tlnp | grep 8080
   
   # Or using lsof
   lsof -i :8080
   ```

2. **Check Application Logs:**
   ```bash
   # Look for successful startup message
   tail -f logs/spring.log | grep "Started Application"
   ```

3. **Test Basic Connectivity:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

---

### Issue 2: "403 Forbidden" or "401 Unauthorized"

**Symptoms:** Swagger page loads but shows authentication error.

**Solution:** This should NOT happen for Swagger UI as it's whitelisted in security config.

If you see this, check `SecurityConfig.java`:

```java
.requestMatchers(
    "/api/auth/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/v3/api-docs",
    "/api-docs/**",
    "/swagger-resources/**",
    "/configuration/ui",
    "/configuration/security",
    "/webjars/**",
    "/actuator/health"
).permitAll()
```

---

### Issue 3: Swagger Loads But Shows "Failed to Load API Definition"

**Symptoms:** Swagger UI page loads, but no APIs are visible.

**Solutions:**

1. **Check OpenAPI Docs Endpoint:**
   ```bash
   curl http://localhost:8080/v3/api-docs
   ```
   
   You should see JSON output with API definitions.

2. **Verify SpringDoc Configuration in `application.yml`:**
   ```yaml
   springdoc:
     api-docs:
       path: /v3/api-docs
       enabled: true
     swagger-ui:
       enabled: true
       path: /swagger-ui.html
     packages-to-scan: dev.amirgol.stockmanager.controller
   ```

3. **Check for Controller Scanning Issues:**
   ```bash
   # Ensure controllers are in the correct package
   find src/main/java -name "*Controller.java"
   ```

4. **Restart the Application:**
   ```bash
   # Stop the application (Ctrl+C)
   # Clean and rebuild
   mvn clean package -DskipTests
   
   # Start again
   mvn spring-boot:run
   ```

---

### Issue 4: "Whitelabel Error Page" or 404

**Symptoms:** Swagger URL returns Spring's default error page.

**Solutions:**

1. **Check if springdoc dependency is in `pom.xml`:**
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.8.5</version>
   </dependency>
   ```

2. **Verify OpenApiConfig exists:**
   ```bash
   ls -la src/main/java/dev/amirgol/stockmanager/config/OpenApiConfig.java
   ```

3. **Check application startup logs for Swagger initialization:**
   ```bash
   # Look for springdoc messages
   grep -i "springdoc\|swagger" logs/spring.log
   ```

---

### Issue 5: Port 8080 Already in Use

**Symptoms:** Application fails to start with "Port 8080 is already in use"

**Solutions:**

1. **Find and kill the process:**
   ```bash
   # Find PID using port 8080
   lsof -ti:8080
   
   # Kill the process
   kill -9 $(lsof -ti:8080)
   ```

2. **Or change the port in `application.yml`:**
   ```yaml
   server:
     port: 8081
   ```
   
   Then access Swagger at: `http://localhost:8081/swagger-ui/index.html`

---

## 🌐 Available Swagger URLs

### Production URLs
```
Main Swagger UI:     http://localhost:8080/swagger-ui/index.html
Alternative:         http://localhost:8080/swagger-ui.html
Short URL:           http://localhost:8080/swagger-ui/

OpenAPI JSON:        http://localhost:8080/v3/api-docs
OpenAPI YAML:        http://localhost:8080/v3/api-docs.yaml

Health Check:        http://localhost:8080/actuator/health
```

### Testing with Different Ports
If you changed the server port:
```
http://localhost:{PORT}/swagger-ui/index.html
```

Example with port 8081:
```
http://localhost:8081/swagger-ui/index.html
```

---

## 📚 Complete API Testing Workflow

Once Swagger UI loads successfully, follow this workflow:

### 1️⃣ Register a New User

**Endpoint:** `POST /api/auth/register`

**No authentication required**

**Request Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "SecurePass123",
  "firstName": "Test",
  "lastName": "User"
}
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY...",
  "type": "Bearer",
  "username": "testuser",
  "roles": ["ROLE_USER"]
}
```

**📝 Important:** Copy the `token` value!

---

### 2️⃣ Authenticate Your Requests

1. **Click the "Authorize" button** 🔓 at the top-right of Swagger UI
2. **In the popup, enter:**
   ```
   Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY...
   ```
   ⚠️ Make sure to include "Bearer " before the token!

3. **Click "Authorize"**
4. **Click "Close"**

🎉 Now all your API requests will include the JWT token automatically!

---

### 3️⃣ Login (Alternative to Registration)

**Endpoint:** `POST /api/auth/login`

**No authentication required**

**Request Body:**
```json
{
  "username": "testuser",
  "password": "SecurePass123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "testuser",
  "roles": ["ROLE_USER"]
}
```

---

### 4️⃣ Create a Product (Requires MANAGER/ADMIN Role)

**Endpoint:** `POST /api/products`

**⚠️ Requires:** MANAGER or ADMIN role (USER role will get 403 Forbidden)

**Request Body:**
```json
{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99
}
```

**Expected Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stock": 100,
  "availableStock": 100,
  "reservedQuantity": null,
  "reservedUntil": null
}
```

**Note:** Initial stock is automatically set to 100.

---

### 5️⃣ Get Product Details (USER Role OK)

**Endpoint:** `GET /api/products/{id}`

**✅ Requires:** USER, MANAGER, or ADMIN role

**Path Parameter:**
- `id`: Product UUID (from create response)

**Expected Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stock": 100,
  "availableStock": 100,
  "reservedQuantity": null,
  "reservedUntil": null
}
```

---

### 6️⃣ Buy Product (USER Role OK)

**Endpoint:** `POST /api/products/{id}/buy`

**✅ Requires:** USER, MANAGER, or ADMIN role

**Path Parameter:**
- `id`: Product UUID

**Query Parameter:**
- `quantity`: Number of items to purchase (integer)

**Example:** `POST /api/products/550e8400-e29b-41d4-a716-446655440000/buy?quantity=5`

**Expected Response (200 OK):**
```json
{
  "totalStock": 95,
  "availableStock": 95,
  "reservedQuantity": null,
  "reservedUntil": null
}
```

**Error Response (400 Bad Request) - Insufficient Stock:**
```json
{
  "type": "about:blank",
  "title": "Insufficient Stock",
  "status": 400,
  "detail": "Insufficient stock. Available: 95, Requested: 200"
}
```

---

### 7️⃣ Reserve Product (USER Role OK)

**Endpoint:** `POST /api/products/{id}/reserve`

**✅ Requires:** USER, MANAGER, or ADMIN role

**Path Parameter:**
- `id`: Product UUID

**Query Parameters:**
- `quantity`: Number of items to reserve (integer)
- `duration`: Reservation duration in minutes (integer)

**Example:** `POST /api/products/550e8400-e29b-41d4-a716-446655440000/reserve?quantity=10&duration=30`

**Expected Response (200 OK):**
```json
{
  "totalStock": 95,
  "availableStock": 85,
  "reservedQuantity": 10,
  "reservedUntil": "2025-10-18T19:30:00"
}
```

**📝 Note:** 
- Reserved stock is automatically released after the duration expires
- Other users cannot buy reserved stock during the reservation period
- Available stock = Total stock - Reserved quantity

---

### 8️⃣ Refill Stock (Requires MANAGER/ADMIN Role)

**Endpoint:** `POST /api/products/{id}/refill`

**⚠️ Requires:** MANAGER or ADMIN role

**Path Parameter:**
- `id`: Product UUID

**Query Parameter:**
- `amount`: Amount to add to stock (integer)

**Example:** `POST /api/products/550e8400-e29b-41d4-a716-446655440000/refill?amount=50`

**Expected Response (200 OK):**
```json
{
  "totalStock": 145,
  "availableStock": 135,
  "reservedQuantity": 10,
  "reservedUntil": "2025-10-18T19:30:00"
}
```

---

### 9️⃣ Get Product Stock Info (USER Role OK)

**Endpoint:** `GET /api/products/{id}/stock`

**✅ Requires:** USER, MANAGER, or ADMIN role

**Path Parameter:**
- `id`: Product UUID

**Expected Response (200 OK):**
```json
{
  "totalStock": 145,
  "availableStock": 135,
  "reservedQuantity": 10,
  "reservedUntil": "2025-10-18T19:30:00"
}
```

---

## 🔐 Role-Based Access Control Summary

| Endpoint | USER | MANAGER | ADMIN | No Auth |
|----------|------|---------|-------|---------|
| `POST /api/auth/register` | ✅ | ✅ | ✅ | ✅ |
| `POST /api/auth/login` | ✅ | ✅ | ✅ | ✅ |
| `GET /api/products/{id}` | ✅ | ✅ | ✅ | ❌ |
| `GET /api/products/{id}/stock` | ✅ | ✅ | ✅ | ❌ |
| `POST /api/products/{id}/buy` | ✅ | ✅ | ✅ | ❌ |
| `POST /api/products/{id}/reserve` | ✅ | ✅ | ✅ | ❌ |
| `POST /api/products` | ❌ | ✅ | ✅ | ❌ |
| `POST /api/products/{id}/refill` | ❌ | ✅ | ✅ | ❌ |

---

## 🛠️ Common Issues and Solutions

### ❌ Error: "Failed to fetch"

**Cause:** CORS issue or network connectivity problem

**Solution:**
1. Check if application is running: `curl http://localhost:8080/actuator/health`
2. Clear browser cache and reload
3. Try incognito/private browsing mode
4. Check CORS configuration in `SecurityConfig.java`

---

### ❌ Error: "Unauthorized" (401)

**Cause:** JWT token is missing, expired, or invalid

**Solutions:**
1. **Re-login** to get a new token (tokens expire after 24 hours)
2. **Check authorization:** Click "Authorize" button and verify token format
3. **Format:** Must be `Bearer <token>` (with space after "Bearer")
4. **Re-enter token:** Copy the entire token from login response

---

### ❌ Error: "Forbidden" (403)

**Cause:** Your role doesn't have permission for this endpoint

**Solutions:**
1. **Check your role:** Look at the login response to see your roles
2. **For MANAGER role:** You need to manually update the database
   ```sql
   -- Connect to database
   docker exec -it stock-manager-db psql -U stockuser -d stockdb
   
   -- Update user role
   UPDATE users SET roles = '{USER,MANAGER}' WHERE username = 'testuser';
   ```
3. **For ADMIN role:**
   ```sql
   UPDATE users SET roles = '{USER,MANAGER,ADMIN}' WHERE username = 'testuser';
   ```

---

### ❌ Error: "Not Found" (404) on Product Endpoints

**Cause:** Product ID doesn't exist in database

**Solutions:**
1. **Create a product first** (requires MANAGER/ADMIN)
2. **Copy the correct UUID** from the create product response
3. **Verify product exists:** Use `GET /api/products/{id}` to check

---

### ❌ Swagger Shows Empty Schema or "Unknown Type"

**Cause:** Controller annotations might be missing or incorrect

**Solution:** Verify controllers have proper annotations:
```java
@RestController
@RequestMapping("/api/products")
@Tag(name = "Stock Product Management")
@SecurityRequirement(name = "Bearer Authentication")
```

---

## 🧪 Testing Scenarios

### Scenario 1: Complete E-commerce Flow

1. **Register as customer:**
   ```
   POST /api/auth/register → Get USER token
   ```

2. **Manager creates product:** (Manual DB role update needed)
   ```
   POST /api/products → Returns product ID
   ```

3. **Customer views product:**
   ```
   GET /api/products/{id}
   ```

4. **Customer reserves product:**
   ```
   POST /api/products/{id}/reserve?quantity=1&duration=15
   ```

5. **Customer completes purchase:**
   ```
   POST /api/products/{id}/buy?quantity=1
   ```

6. **Manager refills stock:**
   ```
   POST /api/products/{id}/refill?amount=50
   ```

---

### Scenario 2: Stock Management

1. **Check current stock:**
   ```
   GET /api/products/{id}/stock
   ```

2. **Multiple users reserve:**
   ```
   User A: POST /reserve?quantity=10&duration=30
   User B: POST /reserve?quantity=5&duration=20
   ```

3. **Check available vs total:**
   ```
   GET /api/products/{id}/stock
   → Shows reduced available stock
   ```

4. **Wait for reservation expiry:**
   ```
   After 30 minutes → Reserved stock auto-released
   ```

---

## 📞 Support

If you continue to experience issues:

1. **Check application logs:**
   ```bash
   tail -f logs/spring.log
   ```

2. **Enable debug logging in `application.yml`:**
   ```yaml
   logging:
     level:
       dev.amirgol.stockmanager: DEBUG
       org.springframework.security: DEBUG
       springdoc: DEBUG
   ```

3. **Verify all dependencies:**
   ```bash
   mvn dependency:tree | grep springdoc
   ```

4. **Clean rebuild:**
   ```bash
   mvn clean install -DskipTests
   ```

---

## ✅ Success Checklist

- [ ] Application starts without errors
- [ ] `http://localhost:8080/actuator/health` returns `{"status":"UP"}`
- [ ] `http://localhost:8080/v3/api-docs` returns JSON
- [ ] `http://localhost:8080/swagger-ui/index.html` loads Swagger UI
- [ ] All API endpoints are visible in Swagger
- [ ] Authentication endpoints work without token
- [ ] Can register and get JWT token
- [ ] Can authorize in Swagger UI with token
- [ ] Can access protected endpoints with token

---

## 🎉 You're Ready!

If all checks pass, you can now:
- ✅ Access Swagger UI
- ✅ Test all APIs interactively
- ✅ View API documentation
- ✅ Execute requests with authentication
- ✅ See request/response examples

**Happy Testing! 🚀**