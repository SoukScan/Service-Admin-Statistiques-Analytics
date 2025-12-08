# ✅ Docker Deployment Checklist

## 📋 Pre-Deployment Verification

### System Requirements
- [ ] Windows 10/11 Pro, Enterprise, or Education (for Docker Desktop)
  - Or Linux/Mac with Docker Desktop
- [ ] 4GB RAM minimum (8GB recommended)
- [ ] 10GB free disk space
- [ ] CPU with virtualization support enabled

### Docker Installation
- [ ] Docker Desktop installed (`docker --version`)
- [ ] Docker Compose >= 2.0 (`docker-compose --version`)
- [ ] Docker daemon running (Docker icon visible in system tray)
- [ ] WSL 2 backend enabled (Windows)

### Network & Ports
- [ ] Port 5432 (PostgreSQL) not in use
- [ ] Port 2181 (Zookeeper) not in use
- [ ] Port 9092 (Kafka) not in use
- [ ] Port 29092 (Kafka external) not in use
- [ ] Port 8090 (App) not in use
- [ ] Internet connectivity for pulling Docker images

### Project Files
- [ ] `Dockerfile` exists
- [ ] `docker-compose.yaml` exists
- [ ] `src/main/resources/application-docker.yaml` exists
- [ ] `.dockerignore` exists
- [ ] `public_key.pem` exists at project root
- [ ] `.mvn/wrapper/maven-wrapper.jar` exists
- [ ] `mvnw` file executable (`chmod +x mvnw` on Linux/Mac)

### Git Status
- [ ] No uncommitted changes (`git status` clean)
- [ ] Latest code pulled (`git pull`)
- [ ] No merge conflicts
- [ ] Branch correct (usually `main` or `develop`)

### Code Quality
- [ ] Project compiles locally (`mvn clean compile`)
- [ ] Tests pass (`mvn test` or review last run)
- [ ] No compilation warnings
- [ ] No syntax errors in Java files

---

## 🔨 Build Phase

### Dockerfile Validation
- [ ] `FROM openjdk:21-jdk-slim` (builder stage)
- [ ] `FROM openjdk:21-jre-slim` (runtime stage)
- [ ] Maven Wrapper used (`./mvnw clean package -DskipTests`)
- [ ] Non-root user created (`appuser`)
- [ ] JAR target: `admin-moderation-service-1.0.0.jar`
- [ ] Port 8090 exposed
- [ ] Health check configured
- [ ] Spring profile flag: `--spring.profiles.active=docker`

### docker-compose.yaml Validation
- [ ] PostgreSQL service configured
  - [ ] Port 5432
  - [ ] Username: `admin`
  - [ ] Password: `admin123`
  - [ ] Database: `admin_db`
  - [ ] Healthcheck present
  - [ ] Volumes: `postgres_data:/var/lib/postgresql/data`

- [ ] Zookeeper service configured
  - [ ] Port 2181
  - [ ] Healthcheck present

- [ ] Kafka service configured
  - [ ] Ports: 9092 (internal), 29092 (external)
  - [ ] Depends on: Zookeeper
  - [ ] Healthcheck present
  - [ ] Auto-create topics: `true`

- [ ] Admin Service configured
  - [ ] Depends on: PostgreSQL, Kafka
  - [ ] Build: Local Dockerfile
  - [ ] Port 8090
  - [ ] Environment variables complete
  - [ ] Volumes: `./public_key.pem:/app/public_key.pem:ro`
  - [ ] Healthcheck: `/actuator/health`
  - [ ] Start period: >= 45s

- [ ] Network configured
  - [ ] Name: `admin-moderation-network`
  - [ ] Driver: `bridge`
  - [ ] All services connected

- [ ] Volumes configured
  - [ ] `postgres_data` (local driver)
  - [ ] `zookeeper_data` (local driver)
  - [ ] `zookeeper_logs` (local driver)
  - [ ] `kafka_data` (local driver)

### application-docker.yaml Validation
- [ ] Datasource URL: `jdbc:postgresql://postgres:5432/admin_db`
- [ ] Database credentials: `admin` / `admin123`
- [ ] Liquibase enabled: `true`
- [ ] Liquibase change-log: `classpath:db/changelog/db.changelog-master.yaml`
- [ ] Kafka bootstrap: `kafka:9092` (NOT localhost:9092)
- [ ] JWT public key: `file:./public_key.pem`
- [ ] Server port: `8090`
- [ ] Profiles active: `docker`
- [ ] JPA ddl-auto: `validate`
- [ ] Hibernat dialect: `PostgreSQLDialect`

---

## 🚀 Deployment Phase

### Build Command
```bash
docker-compose build
```
- [ ] Build completes without errors
- [ ] Image size reasonable (~600MB)
- [ ] No warnings about deprecated layers

### Start Command
```bash
docker-compose up --build
```
- [ ] PostgreSQL starts (healthy)
- [ ] Zookeeper starts (healthy)
- [ ] Kafka starts (healthy)
- [ ] Admin Service starts (UP)
- [ ] No container crashes
- [ ] No port conflicts

### Startup Sequence Check
```
✓ PostgreSQL: "database system is ready to accept connections"
✓ Zookeeper: "binding to port 0.0.0.0/0.0.0.0:2181"
✓ Kafka: "started (kafka.server.KafkaServer)"
✓ Admin Service: "Started AdminServiceApplication in X seconds"
✓ Liquibase: "Total change sets: 4"
```

---

## ✅ Verification Phase

### Service Status
```bash
docker-compose ps
```
- [ ] All services show `Up`
- [ ] Postgres shows `healthy`
- [ ] Zookeeper shows `healthy`
- [ ] Kafka shows `healthy`
- [ ] Admin Service status checks pass

### Health Endpoint
```bash
curl http://localhost:8090/actuator/health
```
- [ ] Returns HTTP 200
- [ ] Status: `"status":"UP"`
- [ ] Response time < 1 second

### Database Connectivity
```bash
docker-compose exec postgres psql -U admin -d admin_db -c "SELECT 1;"
```
- [ ] Returns `1` (success)
- [ ] No connection errors
- [ ] Credentials work

### Kafka Connectivity
```bash
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```
- [ ] Shows broker versions
- [ ] No connection timeout
- [ ] No unknown host errors

### Liquibase Execution
```bash
docker-compose logs admin-moderation-service | grep -i liquibase
```
- [ ] "Reading from public.databasechangelog"
- [ ] "Total change sets: 4"
- [ ] No migration errors
- [ ] No lock timeout issues

### Tables Created
```bash
docker-compose exec postgres psql -U admin -d admin_db -c "\dt"
```
- [ ] `admin_action_log` table exists
- [ ] `vendor_status_history` table exists
- [ ] `moderation_review` table exists
- [ ] `admin_stats_cache` table exists
- [ ] `databasechangelog` table exists (Liquibase)
- [ ] `databasechangeloglock` table exists (Liquibase)

### Logs Inspection
```bash
docker-compose logs admin-moderation-service
```
- [ ] No `ERROR` level messages
- [ ] No `Exception` stacktraces
- [ ] No `Failed to create bean` messages
- [ ] No `Connection refused` messages
- [ ] No `OutOfMemoryError` messages

---

## 🧪 Testing Phase

### REST API Tests
- [ ] `GET /actuator/health` → 200 OK
- [ ] `GET /actuator/metrics` → 200 OK
- [ ] Swagger UI accessible: `http://localhost:8090/swagger-ui.html`

### Database Tests
- [ ] Can connect via psql
- [ ] Can query tables
- [ ] Can insert test data
- [ ] Transactions working

### Kafka Tests
- [ ] Topics auto-created
- [ ] Can produce messages
- [ ] Can consume messages
- [ ] Consumer groups created

### Performance Tests
- [ ] Response time < 500ms for health check
- [ ] No memory leaks (check docker stats)
- [ ] CPU usage reasonable (< 20% per container)
- [ ] Disk space usage acceptable

---

## 🔒 Security Verification

- [ ] Non-root user running app (`appuser`)
- [ ] public_key.pem mounted read-only
- [ ] No hardcoded secrets in environment
- [ ] Docker network is private (not exposed globally)
- [ ] Health checks work (liveness & readiness)
- [ ] No debug mode enabled in production config

---

## 📊 Monitoring Setup

- [ ] Logs directory created: `./logs`
- [ ] Log files being written: `admin-moderation-service.log`
- [ ] Log rotation configured: 10MB max, 30 files
- [ ] Docker stats available: `docker stats`
- [ ] Metrics endpoint working: `GET /actuator/metrics`
- [ ] Health probes accessible: `GET /actuator/health/liveness`

---

## 🛑 Shutdown & Cleanup

- [ ] Stop gracefully: `docker-compose down`
- [ ] Verify containers stopped: `docker-compose ps` (empty)
- [ ] Verify volumes persisted: `docker volume ls`
- [ ] No orphaned processes
- [ ] Port 8090 freed

---

## 📚 Documentation Complete

- [ ] ✅ `Dockerfile` - Generated
- [ ] ✅ `docker-compose.yaml` - Generated
- [ ] ✅ `application-docker.yaml` - Generated
- [ ] ✅ `.dockerignore` - Generated
- [ ] ✅ `DOCKER_DEPLOYMENT_GUIDE.md` - Generated
- [ ] ✅ `DOCKER_QUICK_START.md` - Generated
- [ ] ✅ `DOCKER_SETUP_COMPLETE.md` - Generated
- [ ] ✅ `DOCKER_COMMANDS_WINDOWS.ps1` - Generated
- [ ] ✅ `docker-commands.sh` - Generated
- [ ] ✅ `docker-commands.bat` - Generated

---

## 🎯 Go-Live Checklist

### Pre-Launch (24 hours before)
- [ ] Review all generated files
- [ ] Verify all documentation
- [ ] Test complete deployment flow
- [ ] Check system resources
- [ ] Backup critical data

### Launch Day
- [ ] Final code review
- [ ] All tests passing
- [ ] Team informed of deployment
- [ ] Monitoring setup verified
- [ ] Rollback plan documented

### Launch (Step-by-Step)
- [ ] Announce deployment window
- [ ] Stop any existing instances
- [ ] Run full cleanup: `docker-compose down -v`
- [ ] Run: `docker-compose up --build`
- [ ] Verify all services healthy
- [ ] Test all endpoints
- [ ] Monitor logs for 30 minutes
- [ ] Announce service is live

### Post-Launch
- [ ] Monitor application metrics
- [ ] Check error logs
- [ ] Verify database connectivity
- [ ] Confirm Kafka message flow
- [ ] Collect feedback from team
- [ ] Document any issues
- [ ] Plan improvements

---

## 📞 Quick Troubleshooting

| Issue | Check | Fix |
|-------|-------|-----|
| Port in use | `netstat -ano \| findstr :8090` | Kill process or change port |
| Postgres down | `docker-compose logs postgres` | `docker-compose restart postgres` |
| Liquibase lock | Check logs | `docker-compose down -v && up` |
| Kafka timeout | Bootstrap server URL | Change to `kafka:9092` |
| No data persisting | Volume mapping | Check `docker volume ls` |
| High CPU | Resource limits | Reduce heap size |
| Out of memory | JVM settings | Increase `_JAVA_OPTIONS` |

---

## ✨ Success Criteria

✅ **All containers healthy**
✅ **All services responding**
✅ **Database initialized**
✅ **Liquibase migrations applied**
✅ **Kafka topics created**
✅ **Health check passing**
✅ **Logs clean (no errors)**
✅ **Performance acceptable**
✅ **Documentation complete**
✅ **Team trained on operations**

---

**Checklist Version:** 1.0  
**Last Updated:** 2025-12-08  
**Status:** ✅ Ready for Deployment
