#!/usr/bin/env pwsh
# Docker Compose Quick Start Script for Admin Moderation Service
# Windows PowerShell Version

param(
    [string]$Action = "up",  # up, build, down, logs, status
    [string]$Service = ""    # specific service (optional)
)

$PROJECT_PATH = "C:\Users\MSI\Documents\admin-moderation-service"
$DOCKER_COMPOSE_FILE = Join-Path $PROJECT_PATH "docker-compose.yaml"

function Test-DockerInstalled {
    try {
        $version = docker --version 2>$null
        Write-Host "✅ Docker detected: $version"
        return $true
    } catch {
        Write-Host "❌ Docker not installed or not in PATH"
        Write-Host "Install from: https://www.docker.com/products/docker-desktop"
        return $false
    }
}

function Show-Status {
    Write-Host "`n📊 Service Status:" -ForegroundColor Cyan
    docker-compose ps
}

function Show-Logs {
    if ($Service) {
        Write-Host "`n📋 Logs for $Service:" -ForegroundColor Cyan
        docker-compose logs -f $Service
    } else {
        Write-Host "`n📋 All Service Logs:" -ForegroundColor Cyan
        docker-compose logs -f
    }
}

function Build-Services {
    Write-Host "`n🔨 Building Docker image..." -ForegroundColor Yellow
    docker-compose build --no-cache
    Write-Host "✅ Build complete!" -ForegroundColor Green
}

function Start-Services {
    Write-Host "`n🚀 Starting services..." -ForegroundColor Yellow
    docker-compose up -d
    
    Write-Host "`n⏳ Waiting for services to be healthy..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    
    $retries = 0
    $max_retries = 24  # 2 minutes with 5-second intervals
    
    while ($retries -lt $max_retries) {
        $status = docker-compose exec -T admin-moderation-service curl -s http://localhost:8090/actuator/health 2>$null
        
        if ($status -match "UP") {
            Write-Host "✅ Application is healthy!" -ForegroundColor Green
            Write-Host "`n🌐 Service URLs:" -ForegroundColor Cyan
            Write-Host "  - API: http://localhost:8090"
            Write-Host "  - Swagger UI: http://localhost:8090/swagger-ui.html"
            Write-Host "  - Health: http://localhost:8090/actuator/health"
            Write-Host "  - Database: localhost:5432 (admin/admin123)"
            Write-Host "  - Kafka: localhost:9092"
            return $true
        }
        
        $retries++
        $elapsed = $retries * 5
        Write-Host "  ⏳ Waiting... ($elapsed seconds)" -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
    
    Write-Host "⚠️  Services started but health check timed out" -ForegroundColor Yellow
    Write-Host "Check logs with: docker-compose logs -f admin-moderation-service"
    return $false
}

function Stop-Services {
    Write-Host "`n🛑 Stopping services..." -ForegroundColor Yellow
    docker-compose down
    Write-Host "✅ Services stopped!" -ForegroundColor Green
}

function Clean-Services {
    Write-Host "`n🗑️  Removing all containers and volumes..." -ForegroundColor Yellow
    docker-compose down -v
    Write-Host "✅ Cleanup complete!" -ForegroundColor Green
}

# Main
Set-Location $PROJECT_PATH

if (-not (Test-DockerInstalled)) {
    exit 1
}

Write-Host "`n🐳 Admin Moderation Service - Docker Management" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

switch ($Action.ToLower()) {
    "up" {
        Build-Services
        Start-Services
        Show-Status
    }
    "build" {
        Build-Services
    }
    "start" {
        Start-Services
        Show-Status
    }
    "stop" {
        Stop-Services
    }
    "down" {
        Stop-Services
    }
    "clean" {
        Clean-Services
    }
    "logs" {
        Show-Logs
    }
    "status" {
        Show-Status
    }
    "restart" {
        Stop-Services
        Start-Sleep -Seconds 2
        Start-Services
        Show-Status
    }
    default {
        Write-Host "`n📖 Usage:" -ForegroundColor Yellow
        Write-Host "  .\docker-quick-start.ps1 [action] [service]"
        Write-Host "`n🔧 Actions:"
        Write-Host "  up        - Build and start all services (default)"
        Write-Host "  build     - Build Docker image only"
        Write-Host "  start     - Start services (without rebuild)"
        Write-Host "  stop      - Stop all services"
        Write-Host "  down      - Stop all services (alias for stop)"
        Write-Host "  clean     - Remove all containers and volumes"
        Write-Host "  logs      - Follow logs (add service name to filter)"
        Write-Host "  status    - Show service status"
        Write-Host "  restart   - Restart all services"
        Write-Host "`n💡 Examples:"
        Write-Host "  .\docker-quick-start.ps1 up"
        Write-Host "  .\docker-quick-start.ps1 logs admin-moderation-service"
        Write-Host "  .\docker-quick-start.ps1 status"
    }
}

Write-Host "`n"
