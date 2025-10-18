#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   Stock Manager Application - Restart Script${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}\n"

# Step 1: Stop any running instance
echo -e "${YELLOW}[1/5] Stopping any running Spring Boot application on port 8080...${NC}"
PID=$(lsof -ti:8080 2>/dev/null)
if [ ! -z "$PID" ]; then
    echo -e "      Found process $PID using port 8080"
    kill -9 $PID 2>/dev/null
    sleep 2
    echo -e "${GREEN}      ✓ Process stopped${NC}"
else
    echo -e "      No process found on port 8080"
fi

# Step 2: Check PostgreSQL
echo -e "\n${YELLOW}[2/5] Checking PostgreSQL database...${NC}"
if docker ps | grep -q stock-manager-db; then
    echo -e "${GREEN}      ✓ PostgreSQL is running${NC}"
else
    echo -e "${RED}      ✗ PostgreSQL is not running${NC}"
    echo -e "      Starting PostgreSQL with docker-compose..."
    docker-compose up -d
    echo -e "      Waiting for database to be ready..."
    sleep 5
    echo -e "${GREEN}      ✓ PostgreSQL started${NC}"
fi

# Step 3: Clean and build
echo -e "\n${YELLOW}[3/5] Cleaning and building application...${NC}"
mvn clean package -DskipTests > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}      ✓ Build successful${NC}"
else
    echo -e "${RED}      ✗ Build failed${NC}"
    echo -e "${RED}      Run 'mvn clean package' to see error details${NC}"
    exit 1
fi

# Step 4: Start application in background
echo -e "\n${YELLOW}[4/5] Starting application...${NC}"
nohup mvn spring-boot:run > logs/application.log 2>&1 &
APP_PID=$!
echo -e "      Application starting with PID: $APP_PID"

# Step 5: Wait and verify
echo -e "\n${YELLOW}[5/5] Waiting for application to start...${NC}"
echo -e "      This may take 10-20 seconds...\n"

for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "\n${GREEN}════════════════════════════════════════════════════════════${NC}"
        echo -e "${GREEN}   ✓ Application started successfully!${NC}"
        echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}\n"
        echo -e "${BLUE}Access Swagger UI at:${NC}"
        echo -e "   ${GREEN}http://localhost:8080/swagger-ui/index.html${NC}\n"
        echo -e "${BLUE}Health Check:${NC}"
        echo -e "   ${GREEN}http://localhost:8080/actuator/health${NC}\n"
        echo -e "${BLUE}OpenAPI Docs:${NC}"
        echo -e "   ${GREEN}http://localhost:8080/v3/api-docs${NC}\n"
        echo -e "${BLUE}View logs:${NC}"
        echo -e "   tail -f logs/application.log\n"
        echo -e "${BLUE}Stop application:${NC}"
        echo -e "   kill $APP_PID\n"
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo -e "\n\n${RED}════════════════════════════════════════════════════════════${NC}"
echo -e "${RED}   ✗ Application failed to start within 30 seconds${NC}"
echo -e "${RED}════════════════════════════════════════════════════════════${NC}\n"
echo -e "${YELLOW}Check logs for errors:${NC}"
echo -e "   tail -100 logs/application.log\n"
echo -e "${YELLOW}Or try starting manually:${NC}"
echo -e "   mvn spring-boot:run\n"
exit 1
