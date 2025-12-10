@echo off
REM Docker Compose Quick Start Script for Admin Moderation Service
REM Windows Batch Version

setlocal enabledelayedexpansion

if "%1"=="" (
    set ACTION=up
) else (
    set ACTION=%1
)

set SERVICE=%2
set PROJECT_PATH=C:\Users\MSI\Documents\admin-moderation-service

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker not found. Install from: https://www.docker.com/products/docker-desktop
    exit /b 1
)

echo.
echo 🐳 Admin Moderation Service - Docker Management
echo ==============================================
echo.

REM Change to project directory
cd /d "%PROJECT_PATH%"

REM Handle different actions
if /i "%ACTION%"=="up" (
    echo 🔨 Building Docker image...
    docker-compose build --no-cache
    if %errorlevel% neq 0 goto error
    
    echo.
    echo 🚀 Starting services...
    docker-compose up -d
    if %errorlevel% neq 0 goto error
    
    echo.
    echo ⏳ Waiting for services to be healthy... (this may take 1-2 minutes)
    timeout /t 10
    
    :health_check
    docker-compose exec -T admin-moderation-service curl -s http://localhost:8090/actuator/health >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ Application is healthy!
        echo.
        echo 🌐 Service URLs:
        echo   - API: http://localhost:8090
        echo   - Swagger UI: http://localhost:8090/swagger-ui.html
        echo   - Health: http://localhost:8090/actuator/health
        echo   - Database: localhost:5432 (admin/admin123)
        echo   - Kafka: localhost:9092
        echo.
        docker-compose ps
        goto end
    ) else (
        echo ⚠️  Still waiting for application to start...
        timeout /t 5
        goto health_check
    )
    
) else if /i "%ACTION%"=="build" (
    echo 🔨 Building Docker image...
    docker-compose build --no-cache
    if %errorlevel% neq 0 goto error
    echo ✅ Build complete!
    
) else if /i "%ACTION%"=="start" (
    echo 🚀 Starting services...
    docker-compose up -d
    if %errorlevel% neq 0 goto error
    docker-compose ps
    
) else if /i "%ACTION%"=="stop" (
    echo 🛑 Stopping services...
    docker-compose down
    if %errorlevel% neq 0 goto error
    echo ✅ Services stopped!
    
) else if /i "%ACTION%"=="down" (
    echo 🛑 Stopping services...
    docker-compose down
    if %errorlevel% neq 0 goto error
    echo ✅ Services stopped!
    
) else if /i "%ACTION%"=="clean" (
    echo 🗑️  Removing all containers and volumes...
    docker-compose down -v
    if %errorlevel% neq 0 goto error
    echo ✅ Cleanup complete!
    
) else if /i "%ACTION%"=="logs" (
    echo 📋 Showing logs...
    if "%SERVICE%"=="" (
        docker-compose logs -f
    ) else (
        docker-compose logs -f %SERVICE%
    )
    
) else if /i "%ACTION%"=="status" (
    echo 📊 Service Status:
    docker-compose ps
    
) else if /i "%ACTION%"=="restart" (
    echo 🔄 Restarting services...
    docker-compose down
    timeout /t 2
    docker-compose up -d
    if %errorlevel% neq 0 goto error
    docker-compose ps
    
) else (
    echo 📖 Usage: docker-quick-start.bat [action] [service]
    echo.
    echo 🔧 Actions:
    echo   up        - Build and start all services (default)
    echo   build     - Build Docker image only
    echo   start     - Start services without rebuild
    echo   stop      - Stop all services
    echo   down      - Stop all services (alias for stop)
    echo   clean     - Remove all containers and volumes
    echo   logs      - Follow logs (add service name to filter)
    echo   status    - Show service status
    echo   restart   - Restart all services
    echo.
    echo 💡 Examples:
    echo   docker-quick-start.bat up
    echo   docker-quick-start.bat logs admin-moderation-service
    echo   docker-quick-start.bat status
    goto end
)

goto end

:error
echo.
echo ❌ Error occurred! Check the output above.
exit /b 1

:end
echo.
exit /b 0
