# Installation Guide - Event-Driven Order Processing System

## Prerequisites Check

Before building the project, ensure you have the following installed:

### 1. Java 17 or Higher

**Check if Java is installed:**
```powershell
java -version
```

**Expected output:**
```
java version "17.0.x" or higher
```

**If not installed, download from:**
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java17
- OpenJDK: https://adoptium.net/

**Windows Installation:**
1. Download Java 17 installer
2. Run the installer
3. Add to PATH (installer usually does this)
4. Set JAVA_HOME environment variable:
   ```powershell
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-17', 'Machine')
   ```
5. Restart PowerShell

### 2. Docker Desktop

**Check if Docker is installed:**
```powershell
docker --version
docker-compose --version
```

**If not installed, download from:**
https://www.docker.com/products/docker-desktop/

**Windows Installation:**
1. Download Docker Desktop for Windows
2. Run the installer
3. Enable WSL 2 if prompted
4. Restart your computer
5. Start Docker Desktop

## Building the Project

### Option 1: Using Maven Wrapper (Recommended - No Maven installation needed)

The project includes Maven Wrapper, so you don't need to install Maven separately.

**Windows (PowerShell):**
```powershell
# Build all services
.\mvnw.cmd clean package -DskipTests
```

**Linux/Mac:**
```bash
# Build all services
./mvnw clean package -DskipTests
```

### Option 2: Using System Maven (If already installed)

**Check if Maven is installed:**
```powershell
mvn -version
```

**If Maven is installed:**
```powershell
mvn clean package -DskipTests
```

**To install Maven on Windows:**
1. Download from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH:
   ```powershell
   $env:Path += ";C:\Program Files\Apache\maven\bin"
   ```
4. Set M2_HOME:
   ```powershell
   [System.Environment]::SetEnvironmentVariable('M2_HOME', 'C:\Program Files\Apache\maven', 'Machine')
   ```
5. Restart PowerShell

## Step-by-Step Setup

### Step 1: Verify Java Installation

```powershell
java -version
```

If you see a version number (17+), you're good to go!

### Step 2: Build the Project

```powershell
# Navigate to project directory
cd "C:\Users\adith\Downloads\Event Driven Order Processing System"

# Build using Maven Wrapper (recommended)
.\mvnw.cmd clean package -DskipTests
```

This will:
- Download Maven automatically (first run only)
- Compile all 6 microservices
- Create JAR files in each service's `target/` folder
- Takes about 2-5 minutes on first run

### Step 3: Start Docker Desktop

Make sure Docker Desktop is running before proceeding.

### Step 4: Start All Services

```powershell
# Start all services with Docker Compose
docker-compose up -d
```

This will:
- Pull required Docker images (PostgreSQL, Redis, Kafka, etc.)
- Build Docker images for your services
- Start 10 containers
- Takes about 3-5 minutes on first run

### Step 5: Verify Services are Running

**Check containers:**
```powershell
docker-compose ps
```

All services should show "Up" status.

**Check Eureka Dashboard:**
Open browser: http://localhost:8761

You should see all 4 services registered (order, payment, inventory, notification).

### Step 6: Test the System

**Add inventory:**
```powershell
curl http://localhost:8083/api/inventory?productId=PROD001&productName=iPhone%2015%20Pro&quantity=100 -Method POST
```

**Create an order:**
```powershell
$body = @{
    items = @(
        @{
            productId = "PROD001"
            productName = "iPhone 15 Pro"
            quantity = 1
            price = 999.99
        }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/orders" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"; "X-User-Id"="user123"} `
    -Body $body
```

**View logs:**
```powershell
docker-compose logs -f
```

Press `Ctrl+C` to stop viewing logs.

## Troubleshooting

### Issue: "mvn: command not found" or "mvnw.cmd: command not found"

**Solution:** Use the Maven Wrapper with .\mvnw.cmd on Windows:
```powershell
.\mvnw.cmd clean package -DskipTests
```

### Issue: "JAVA_HOME not found"

**Solution:** Set JAVA_HOME environment variable:
```powershell
# Find Java installation
$javaPath = (Get-Command java).Source
$javaHome = Split-Path (Split-Path $javaPath) -Parent

# Set JAVA_HOME
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'Machine')

# Restart PowerShell
```

### Issue: "Docker is not running"

**Solution:** 
1. Start Docker Desktop application
2. Wait for it to fully start (green icon in system tray)
3. Try again

### Issue: "Port already in use"

**Solution:** Stop conflicting services:
```powershell
# Check what's using the port
netstat -ano | findstr :8080

# Or use different ports in docker-compose.yml
```

### Issue: Build fails with "compilation error"

**Solution:**
```powershell
# Clean everything and rebuild
.\mvnw.cmd clean
.\mvnw.cmd clean install -DskipTests
```

### Issue: Services not registering with Eureka

**Solution:**
- Wait 1-2 minutes for services to register
- Check logs: `docker-compose logs eureka-server`
- Restart services: `docker-compose restart`

## Quick Reference Commands

```powershell
# Build project
.\mvnw.cmd clean package -DskipTests

# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f order-service

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Rebuild and restart
docker-compose up -d --build

# Check service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

## Alternative: Using IntelliJ IDEA or Eclipse

If you prefer using an IDE:

1. **Import as Maven Project**
2. **Let IDE download dependencies**
3. **Run each service's main class:**
   - `EurekaServerApplication`
   - `ApiGatewayApplication`
   - `OrderServiceApplication`
   - `PaymentServiceApplication`
   - `InventoryServiceApplication`
   - `NotificationServiceApplication`

4. **Start infrastructure separately:**
   ```powershell
   docker-compose up -d postgres redis kafka zookeeper
   ```

## System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| RAM | 8 GB | 16 GB |
| CPU | 4 cores | 8 cores |
| Disk Space | 10 GB | 20 GB |
| OS | Windows 10/11, Linux, macOS | - |
| Java | 17 | 17 or 21 |
| Docker Desktop | 4.x | Latest |

## Next Steps

Once everything is running, check out:
- [README.md](README.md) - Complete documentation
- [TEST_REQUESTS.md](TEST_REQUESTS.md) - API testing examples
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Architecture overview

## Getting Help

If you encounter issues not covered here:
1. Check service logs: `docker-compose logs -f [service-name]`
2. Verify all prerequisites are installed
3. Ensure Docker Desktop is running
4. Make sure ports 8080-8084, 5432, 6379, 9092 are not in use
