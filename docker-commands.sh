#!/bin/bash

# Admin Moderation Service - Docker Helper Script
# Usage: ./docker-commands.sh [command]

set -e

PROJECT_NAME="admin-moderation-service"
DOCKER_IMAGE="$PROJECT_NAME:latest"
SERVICE_URL="http://localhost:8090"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Commands
cmd_build() {
    print_header "Building Docker Image"
    docker-compose build
    print_success "Build complete!"
}

cmd_up() {
    print_header "Starting Services (with logs)"
    docker-compose up --build
}

cmd_up_background() {
    print_header "Starting Services (background)"
    docker-compose up --build -d
    sleep 5
    cmd_status
}

cmd_down() {
    print_header "Stopping Services"
    docker-compose down
    print_success "Services stopped"
}

cmd_down_volumes() {
    print_header "Stopping Services and Removing Volumes"
    docker-compose down -v
    print_success "Services stopped and volumes removed"
}

cmd_status() {
    print_header "Service Status"
    docker-compose ps
}

cmd_logs() {
    print_header "Logs - All Services (press Ctrl+C to stop)"
    docker-compose logs -f
}

cmd_logs_app() {
    print_header "Logs - Admin Service (press Ctrl+C to stop)"
    docker-compose logs -f admin-moderation-service
}

cmd_logs_db() {
    print_header "Logs - PostgreSQL (press Ctrl+C to stop)"
    docker-compose logs -f postgres
}

cmd_logs_kafka() {
    print_header "Logs - Kafka (press Ctrl+C to stop)"
    docker-compose logs -f kafka
}

cmd_restart() {
    print_header "Restarting All Services"
    docker-compose restart
    print_success "Services restarted"
}

cmd_restart_app() {
    print_header "Restarting Admin Service"
    docker-compose restart admin-moderation-service
    print_success "Service restarted"
}

cmd_health() {
    print_header "Health Check"
    
    echo -e "${BLUE}Checking service health...${NC}"
    
    if curl -s "$SERVICE_URL/actuator/health" | grep -q '"status":"UP"'; then
        print_success "Admin Moderation Service is UP"
    else
        print_error "Admin Moderation Service is DOWN"
        return 1
    fi
    
    echo ""
    echo -e "${BLUE}Full health details:${NC}"
    curl -s "$SERVICE_URL/actuator/health" | jq '.'
}

cmd_db_shell() {
    print_header "PostgreSQL Shell"
    docker-compose exec postgres psql -U admin -d admin_db
}

cmd_app_shell() {
    print_header "Application Container Shell"
    docker-compose exec admin-moderation-service /bin/bash
}

cmd_test_swagger() {
    print_header "Swagger/OpenAPI"
    echo -e "${BLUE}Open your browser:${NC}"
    echo "http://localhost:8090/swagger-ui.html"
}

cmd_test_health() {
    print_header "Testing Health Endpoint"
    echo -e "${BLUE}GET /actuator/health${NC}"
    curl -s "$SERVICE_URL/actuator/health" | jq '.'
}

cmd_test_metrics() {
    print_header "Testing Metrics Endpoint"
    echo -e "${BLUE}GET /actuator/metrics${NC}"
    curl -s "$SERVICE_URL/actuator/metrics" | jq '.'
}

cmd_stats() {
    print_header "Docker Stats (press Ctrl+C to stop)"
    docker stats
}

cmd_clean() {
    print_header "Deep Clean"
    print_warning "This will remove all containers, images, and volumes!"
    read -p "Continue? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v --remove-orphans
        docker system prune -a -f
        print_success "System cleaned"
    else
        print_warning "Cancelled"
    fi
}

cmd_help() {
    echo -e "${BLUE}Admin Moderation Service - Docker Helper${NC}"
    echo ""
    echo "Usage: ./docker-commands.sh [command]"
    echo ""
    echo -e "${YELLOW}Build & Deploy:${NC}"
    echo "  build                 - Build Docker image"
    echo "  up                    - Start all services with logs"
    echo "  up-bg                 - Start all services in background"
    echo "  down                  - Stop services"
    echo "  down-v                - Stop services and remove volumes"
    echo "  restart               - Restart all services"
    echo "  restart-app           - Restart app only"
    echo ""
    echo -e "${YELLOW}Monitoring & Logs:${NC}"
    echo "  status                - Show service status"
    echo "  logs                  - Show all logs (live)"
    echo "  logs-app              - Show app logs (live)"
    echo "  logs-db               - Show DB logs (live)"
    echo "  logs-kafka            - Show Kafka logs (live)"
    echo "  stats                 - Show resource stats (live)"
    echo "  health                - Check service health"
    echo ""
    echo -e "${YELLOW}Access & Shell:${NC}"
    echo "  shell-db              - PostgreSQL shell"
    echo "  shell-app             - App container shell"
    echo ""
    echo -e "${YELLOW}Testing:${NC}"
    echo "  test-health           - Test /actuator/health"
    echo "  test-metrics          - Test /actuator/metrics"
    echo "  test-swagger          - Open Swagger UI"
    echo ""
    echo -e "${YELLOW}Cleanup:${NC}"
    echo "  clean                 - Deep clean (remove all)"
    echo "  help                  - Show this help"
    echo ""
}

# Main
case "${1:-help}" in
    build)
        cmd_build
        ;;
    up)
        cmd_up
        ;;
    up-bg)
        cmd_up_background
        ;;
    down)
        cmd_down
        ;;
    down-v)
        cmd_down_volumes
        ;;
    status)
        cmd_status
        ;;
    logs)
        cmd_logs
        ;;
    logs-app)
        cmd_logs_app
        ;;
    logs-db)
        cmd_logs_db
        ;;
    logs-kafka)
        cmd_logs_kafka
        ;;
    restart)
        cmd_restart
        ;;
    restart-app)
        cmd_restart_app
        ;;
    stats)
        cmd_stats
        ;;
    health)
        cmd_health
        ;;
    shell-db)
        cmd_db_shell
        ;;
    shell-app)
        cmd_app_shell
        ;;
    test-swagger)
        cmd_test_swagger
        ;;
    test-health)
        cmd_test_health
        ;;
    test-metrics)
        cmd_test_metrics
        ;;
    clean)
        cmd_clean
        ;;
    help|--help|-h)
        cmd_help
        ;;
    *)
        print_error "Unknown command: $1"
        cmd_help
        exit 1
        ;;
esac
