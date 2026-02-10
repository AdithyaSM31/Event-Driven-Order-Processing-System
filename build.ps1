# Build Script for Event-Driven Order Processing System
# This script downloads Maven if needed and builds all services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building Order Processing System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java installation
Write-Host "[Step 1/4] Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "Java not found! Please install Java 17 or higher." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# Download Maven if not exists
$mavenDir = "$env:USERPROFILE\.m2\wrapper\apache-maven-3.9.6"
$mavenBin = "$mavenDir\bin\mvn.cmd"

if (-Not (Test-Path $mavenBin)) {
    Write-Host "[Step 2/4] Downloading Maven 3.9.6..." -ForegroundColor Yellow
    $mavenZip = "$env:TEMP\maven.zip"
    $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    
    try {
        Invoke-WebRequest -Uri $downloadUrl -OutFile $mavenZip -UseBasicParsing
        Write-Host "Maven downloaded" -ForegroundColor Green
        
        Write-Host "Extracting Maven..." -ForegroundColor Yellow
        $extractDir = "$env:USERPROFILE\.m2\wrapper"
        New-Item -ItemType Directory -Force -Path $extractDir | Out-Null
        Expand-Archive -Path $mavenZip -DestinationPath $extractDir -Force
        Remove-Item $mavenZip
        Write-Host "Maven extracted to $mavenDir" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download Maven: $_" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[Step 2/4] Maven already installed" -ForegroundColor Green
}

# Clean previous builds
Write-Host "[Step 3/4] Cleaning previous builds..." -ForegroundColor Yellow
& $mavenBin clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean failed" -ForegroundColor Red
    exit 1
}
Write-Host "Clean complete" -ForegroundColor Green

# Build all services
Write-Host "[Step 4/4] Building all services (this may take 3-5 minutes)..." -ForegroundColor Yellow
& $mavenBin package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Start services: docker-compose up -d" -ForegroundColor White
Write-Host "  2. Check status:   docker-compose ps" -ForegroundColor White
Write-Host "  3. View logs:      docker-compose logs -f" -ForegroundColor White
Write-Host ""
Write-Host "Services will be available at:" -ForegroundColor Cyan
Write-Host "  - Eureka Dashboard:  http://localhost:8761" -ForegroundColor White
Write-Host "  - API Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "  - Order Service:     http://localhost:8081" -ForegroundColor White
Write-Host "  - Payment Service:   http://localhost:8082" -ForegroundColor White
Write-Host "  - Inventory Service: http://localhost:8083" -ForegroundColor White
Write-Host "  - Notification Svc:  http://localhost:8084" -ForegroundColor White
Write-Host ""
