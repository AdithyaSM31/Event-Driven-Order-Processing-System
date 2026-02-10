@echo off
echo ====================================
echo Building Event-Driven Order System
echo ====================================
echo.

echo [1/3] Cleaning previous builds...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed
    exit /b 1
)

echo.
echo [2/3] Packaging all services...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed
    exit /b 1
)

echo.
echo [3/3] Build complete!
echo.
echo Next steps:
echo   1. Start services: docker-compose up -d
echo   2. Check health: docker-compose ps
echo   3. View logs: docker-compose logs -f
echo.
echo Services will be available at:
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway: http://localhost:8080
echo   - Order Service: http://localhost:8081
echo   - Payment Service: http://localhost:8082
echo   - Inventory Service: http://localhost:8083
echo   - Notification Service: http://localhost:8084
echo.
