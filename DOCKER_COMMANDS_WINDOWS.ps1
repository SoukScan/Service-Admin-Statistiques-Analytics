# Windows PowerShell - Docker Commands Cheat Sheet

# ==================================================
# INITIAL SETUP & BUILD
# ==================================================

# 1. Verify Docker is installed
docker --version
docker-compose --version

# 2. Navigate to project root
cd C:\Users\MSI\Documents\admin-moderation-service

# 3. Build Docker image (first time only)
docker-compose build

# ==================================================
# START SERVICES
# ==================================================

# Option A: Start with live logs (blocking)
docker-compose up --build

# Option B: Start in background
docker-compose up --build -d
Start-Sleep -Seconds 5
docker-compose ps

# ==================================================
# VIEW LOGS
# ==================================================

# All services (live)
docker-compose logs -f

# Admin Service only (live)
docker-compose logs -f admin-moderation-service

# PostgreSQL only (live)
docker-compose logs -f postgres

# Kafka only (live)
docker-compose logs -f kafka

# Last 100 lines
docker-compose logs --tail=100

# ==================================================
# CHECK STATUS
# ==================================================

# Service status
docker-compose ps

# Resource usage (live)
docker stats

# ==================================================
# TEST ENDPOINTS
# ==================================================

# Health check (should return {"status":"UP"})
Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET | ConvertTo-Json

# Metrics
Invoke-WebRequest -Uri "http://localhost:8090/actuator/metrics" -Method GET | ConvertTo-Json

# Open Swagger UI in browser
Start-Process "http://localhost:8090/swagger-ui.html"

# ==================================================
# DATABASE OPERATIONS
# ==================================================

# Open PostgreSQL shell
docker-compose exec postgres psql -U admin -d admin_db

# List tables (from PowerShell, then run in psql):
# \dt
# SELECT * FROM databasechangelog;
# \q

# Run SQL query from PowerShell
docker-compose exec postgres psql -U admin -d admin_db -c "SELECT version();"

# ==================================================
# KAFKA OPERATIONS
# ==================================================

# List Kafka topics
docker-compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Create a test topic
docker-compose exec kafka kafka-topics.sh `
  --create `
  --topic test-topic `
  --bootstrap-server localhost:9092 `
  --partitions 1 `
  --replication-factor 1

# ==================================================
# RESTART & TROUBLESHOOT
# ==================================================

# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart admin-moderation-service

# Stop services (keep volumes)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove everything (volumes too)
docker-compose down -v

# Deep clean
docker-compose down -v --remove-orphans
docker system prune -a -f

# ==================================================
# SHELL ACCESS
# ==================================================

# Open bash in app container
docker-compose exec admin-moderation-service /bin/bash

# Open cmd in app container (if Windows base image)
docker-compose exec admin-moderation-service cmd

# Open PostgreSQL shell
docker-compose exec postgres psql -U admin -d admin_db

# ==================================================
# PORT TROUBLESHOOTING
# ==================================================

# Check if port is in use
netstat -ano | findstr ":8090"
netstat -ano | findstr ":5432"
netstat -ano | findstr ":9092"

# Kill process using port (if needed)
$pid = (netstat -ano | findstr ":8090" | ForEach-Object { $_.Split()[-1] })
taskkill /PID $pid /F

# ==================================================
# USEFUL ONE-LINERS
# ==================================================

# Wait for service to be healthy
while ((Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET).StatusCode -ne 200) { 
    Write-Host "Waiting for service..."
    Start-Sleep -Seconds 2 
}
Write-Host "Service is UP!"

# Check all services health
docker-compose ps | Select-String "healthy"

# Get container IDs
docker-compose ps -q

# View environment variables of container
docker inspect admin-moderation-service | ConvertFrom-Json | Select-Object -Property @{
    Name="Container"; Expression={$_.Name}
}, @{
    Name="Environment"; Expression={$_.Config.Env}
} | Format-List

# ==================================================
# COMMON WORKFLOWS
# ==================================================

# WORKFLOW 1: Fresh start
docker-compose down -v
docker-compose up --build

# WORKFLOW 2: Quick restart (dev iteration)
docker-compose restart admin-moderation-service
docker-compose logs -f admin-moderation-service

# WORKFLOW 3: Full system health check
Write-Host "Checking services..." -ForegroundColor Green
docker-compose ps
Write-Host "`nChecking health..." -ForegroundColor Green
Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET | ConvertTo-Json
Write-Host "`nChecking PostgreSQL..." -ForegroundColor Green
docker-compose exec postgres psql -U admin -d admin_db -c "SELECT 'PostgreSQL OK';"
Write-Host "`nChecking Kafka..." -ForegroundColor Green
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# WORKFLOW 4: Continuous development
# Terminal 1 - Watch logs
docker-compose logs -f admin-moderation-service

# Terminal 2 - Make code changes, rebuild, restart
# Make changes in VS Code...
# Then run:
docker-compose restart admin-moderation-service
# Changes will be picked up on restart

# WORKFLOW 5: Full cleanup and rebuild
$confirmation = Read-Host "This will remove all containers, images, and volumes. Continue? (y/n)"
if ($confirmation -eq 'y') {
    docker-compose down -v --remove-orphans
    docker system prune -a -f
    docker-compose up --build
} else {
    Write-Host "Cancelled"
}

# ==================================================
# PERFORMANCE TESTING
# ==================================================

# Test response time
Measure-Command {
    Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET | Out-Null
} | Select-Object -Property @{Name="Duration (ms)"; Expression={$_.TotalMilliseconds}}

# Continuous health polling (every 5 seconds)
while ($true) {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET
    $time = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$time] Status: $($response.StatusCode)"
    Start-Sleep -Seconds 5
}

# ==================================================
# LOGGING & DEBUGGING
# ==================================================

# Export logs to file
docker-compose logs > "logs_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"

# Export service-specific logs
docker-compose logs admin-moderation-service > "service_logs.txt"

# View Liquibase migration logs
docker-compose logs admin-moderation-service | Select-String "liquibase"

# View startup errors only
docker-compose logs admin-moderation-service | Select-String -Pattern "ERROR|Exception|Failed"

# ==================================================
# ADVANCED
# ==================================================

# View Docker network details
docker network ls
docker network inspect admin-moderation_admin-moderation-network

# Check container resource limits
docker inspect admin-moderation-service | ConvertFrom-Json | Select-Object -Property @{
    Name="Memory"; Expression={$_.HostConfig.Memory}
}, @{
    Name="CpuShares"; Expression={$_.HostConfig.CpuShares}
}

# Prune unused Docker resources
docker system prune

# View image sizes
docker images | grep admin-moderation

# ==================================================
# COMPLETE WORKFLOW EXAMPLE (Copy & Paste)
# ==================================================

<#
# Step 1: Setup
cd C:\Users\MSI\Documents\admin-moderation-service
docker-compose down -v

# Step 2: Build and start
docker-compose up --build -d

# Step 3: Wait for startup
Start-Sleep -Seconds 10

# Step 4: Verify
docker-compose ps
Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -Method GET

# Step 5: View logs
docker-compose logs -f admin-moderation-service

# Step 6: Test endpoints
Invoke-WebRequest -Uri "http://localhost:8090/actuator/metrics" -Method GET

# Step 7: Clean shutdown
docker-compose down
#>

# ==================================================
# REFERENCES
# ==================================================

# Full documentation: See DOCKER_DEPLOYMENT_GUIDE.md
# Quick start: See DOCKER_QUICK_START.md
# Setup summary: See DOCKER_SETUP_COMPLETE.md
