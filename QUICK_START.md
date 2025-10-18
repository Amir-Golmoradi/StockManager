# 🚀 Quick Start Guide - Stock Manager API

## ⚡ Fastest Way to Get Started

### 1. Start the Application

```bash
cd /home/amirgol/Downloads/StockManager

# Option A: Use the restart script (Recommended)
./restart-app.sh

# Option B: Manual start
mvn spring-boot:run
```

### 2. Access Swagger UI

Open your browser and go to:

```
http://localhost:8080/swagger-ui/index.html
```

### 3. Test It's Working

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

---

## 🎯 Your First API Call (No Auth Required)

### Register a New User

1. In Swagger UI, find **"Authentication"** section
2. Click on **`POST /api/auth/register`**
3. Click **"Try it out"**
4. Use this JSON:

```json
{
  "username": "demo_user",
  "email": "demo@example.com",
  "password": "DemoPass123",
  "firstName": "Demo",
  "lastName": "User"
}
```

5. Click **"Execute"**
6. **Copy the token** from the response!

---

## 🔐 Authorize All Requests

1. Click the **"Authorize"** button (🔓) at the top
2. Enter: `Bearer YOUR_TOKEN_HERE`
3. Click **"Authorize"** → **"Close"**

✅ Now you can test all protected endpoints!

---

## 📋 Quick Test Checklist

- [ ] Application running on port 8080
- [ ] Swagger UI loads: http://localhost:8080/swagger-ui/index.html
- [ ] Can see all API endpoints
- [ ] Register user works
- [ ] Got JWT token
- [ ] Authorized in Swagger UI
- [ ] Can call protected endpoints

---

## 🛑 Troubleshooting

### Swagger Won't Load?

```bash
# 1. Check if app is running
curl http://localhost:8080/actuator/health

# 2. Check if port is in use
lsof -i :8080

# 3. Restart the application
./restart-app.sh

# 4. Check logs
tail -f logs/application.log
```

### Still Not Working?

1. **Stop all processes:**
   ```bash
   kill -9 $(lsof -ti:8080)
   ```

2. **Rebuild:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start fresh:**
   ```bash
   mvn spring-boot:run
   ```

4. **Wait 15 seconds** then try Swagger again

---

## 📚 Available URLs

| Purpose | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html |
| **API Docs (JSON)** | http://localhost:8080/v3/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |

---

## 🎮 Quick API Examples

### 1. Register (No Auth)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123456"
  }'
```

### 2. Login (No Auth)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'
```

### 3. Get Product (Requires Auth)
```bash
curl -X GET http://localhost:8080/api/products/{product-id} \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4. Buy Product (Requires Auth)
```bash
curl -X POST "http://localhost:8080/api/products/{product-id}/buy?quantity=5" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🔑 User Roles

| Role | Can Do |
|------|--------|
| **USER** | Register, Login, View Products, Buy, Reserve |
| **MANAGER** | Everything USER can + Create Products, Refill Stock |
| **ADMIN** | Everything MANAGER can + Full System Access |

**Note:** New registrations get USER role by default.

To create MANAGER/ADMIN users, update the database:
```sql
docker exec -it stock-manager-db psql -U stockuser -d stockdb
UPDATE users SET roles = '{USER,MANAGER,ADMIN}' WHERE username = 'your_username';
```

---

## 🆘 Need More Help?

See detailed guides:
- **[SWAGGER_GUIDE.md](SWAGGER_GUIDE.md)** - Complete Swagger troubleshooting
- **[README.md](README.md)** - Full project documentation

---

## ✅ Success!

If you can see Swagger UI and execute API calls, **you're all set!** 🎉

**Next Steps:**
1. Explore all endpoints in Swagger
2. Test the authentication flow
3. Try creating and managing products
4. Test the reservation system

**Happy Coding! 🚀**