@echo off
REM Admin Moderation Service - Docker Helper Script (Windows)
REM Usage: docker-commands.bat [command]

setlocal enabledelayedexpansion

set "PROJECT_NAME=admin-moderation-service"
set "SERVICE_URL=http://localhost:8090"

REM ============ COMMANDS ============

if "%1%"=="" goto help
if "%1%"=="help" goto help
if "%1%"=="--help" goto help
if "%1%"=="-h" goto help

if "%1%"=="build" goto build
if "%1%"=="up" goto up
if "%1%"=="up-bg" goto up_bg
if "%1%"=="down" goto down
if "%1%"=="down-v" goto down_v
if "%1%"=="status" goto status
if "%1%"=="logs" goto logs
if "%1%"=="logs-app" goto logs_app
if "%1%"=="logs-db" goto logs_db
if "%1%"=="logs-kafka" goto logs_kafka
if "%1%"=="restart" goto restart
if "%1%"=="restart-app" goto restart_app
if "%1%"=="stats" goto stats
if "%1%"=="health" goto health
if "%1%"=="shell-db" goto shell_db
if "%1%"=="shell-app" goto shell_app
if "%1%"=="test-health" goto test_health
if "%1%"=="test-metrics" goto test_metrics
if "%1%"=="test-swagger" goto test_swagger
if "%1%"=="clean" goto clean

echo Unknown command: %1%
goto help

:build
cls
echo ================================================
echo Building Docker Image
echo ================================================
docker-compose build
echo.
echo [OK] Build complete!
goto end

:up
cls
echo ================================================
echo Starting Services (with logs)
echo ================================================
docker-compose up --build
goto end

:up_bg
cls
echo ================================================
echo Starting Services (background)
echo ================================================
docker-compose up --build -d
timeout /t 5 /nobreak
cls
goto status

:down
cls
echo ================================================
echo Stopping Services
echo ================================================
docker-compose down
echo.
echo [OK] Services stopped
goto end

:down_v
cls
echo ================================================
echo Stopping Services and Removing Volumes
echo ================================================
docker-compose down -v
echo.
echo [OK] Services stopped and volumes removed
goto end

:status
cls
echo ================================================
echo Service Status
echo ================================================
docker-compose ps
echo.
goto end

:logs
cls
echo ================================================
echo Logs - All Services (press Ctrl+C to stop)
echo ================================================
docker-compose logs -f
goto end

:logs_app
cls
echo ================================================
echo Logs - Admin Service (press Ctrl+C to stop)
echo ================================================
docker-compose logs -f admin-moderation-service
goto end

:logs_db
cls
echo ================================================
echo Logs - PostgreSQL (press Ctrl+C to stop)
echo ================================================
docker-compose logs -f postgres
goto end

:logs_kafka
cls
echo ================================================
echo Logs - Kafka (press Ctrl+C to stop)
echo ================================================
docker-compose logs -f kafka
goto end

:restart
cls
echo ================================================
echo Restarting All Services
echo ================================================
docker-compose restart
echo.
echo [OK] Services restarted
goto end

:restart_app
cls
echo ================================================
echo Restarting Admin Service
echo ================================================
docker-compose restart admin-moderation-service
echo.
echo [OK] Service restarted
goto end

:stats
cls
echo ================================================
echo Docker Stats (press Ctrl+C to stop)
echo ================================================
docker stats
goto end

:health
cls
echo ================================================
echo Health Check
echo ================================================
echo.
echo Checking service health...
echo.
curl -s "%SERVICE_URL%/actuator/health"
echo.
echo.
goto end

:shell_db
cls
echo ================================================
echo PostgreSQL Shell
echo ================================================
docker-compose exec postgres psql -U admin -d admin_db
goto end

:shell_app
cls
echo ================================================
echo Application Container Shell
echo ================================================
docker-compose exec admin-moderation-service cmd
goto end

:test_health
cls
echo ================================================
echo Testing Health Endpoint
echo ================================================
echo GET /actuator/health
echo.
curl -s "%SERVICE_URL%/actuator/health"
echo.
echo.
goto end

:test_metrics
cls
echo ================================================
echo Testing Metrics Endpoint
echo ================================================
echo GET /actuator/metrics
echo.
curl -s "%SERVICE_URL%/actuator/metrics"
echo.
echo.
goto end

:test_swagger
cls
echo ================================================
echo Swagger/OpenAPI
echo ================================================
echo.
echo Open your browser:
echo http://localhost:8090/swagger-ui.html
echo.
start http://localhost:8090/swagger-ui.html
goto end

:clean
cls
echo ================================================
echo Deep Clean
echo ================================================
echo WARNING: This will remove all containers, images, and volumes!
set /p confirm="Continue? (y/N): "
if /i "%confirm%"=="y" (
    docker-compose down -v --remove-orphans
    docker system prune -a -f
    echo.
    echo [OK] System cleaned
) else (
    echo Cancelled
)
goto end

:help
cls
echo ================================================
echo Admin Moderation Service - Docker Helper
echo ================================================
echo.
echo Usage: docker-commands.bat [command]
echo.
echo Build ^& Deploy:
echo   build                 - Build Docker image
echo   up                    - Start all services with logs
echo   up-bg                 - Start all services in background
echo   down                  - Stop services
echo   down-v                - Stop services and remove volumes
echo   restart               - Restart all services
echo   restart-app           - Restart app only
echo.
echo Monitoring ^& Logs:
echo   status                - Show service status
echo   logs                  - Show all logs (live)
echo   logs-app              - Show app logs (live)
echo   logs-db               - Show DB logs (live)
echo   logs-kafka            - Show Kafka logs (live)
echo   stats                 - Show resource stats (live)
echo   health                - Check service health
echo.
echo Access ^& Shell:
echo   shell-db              - PostgreSQL shell
echo   shell-app             - App container shell
echo.
echo Testing:
echo   test-health           - Test /actuator/health
echo   test-metrics          - Test /actuator/metrics
echo   test-swagger          - Open Swagger UI
echo.
echo Cleanup:
echo   clean                 - Deep clean (remove all)
echo   help                  - Show this help
echo.

:end
pause
