# 🐳 Docker Deployment Guide - Admin Moderation Service

## 📋 Fichiers Générés

```
admin-moderation-service/
├── Dockerfile                           ← Multi-stage build (Java 21)
├── docker-compose.yaml                  ← Stack complet (PostgreSQL + Kafka + Zookeeper)
├── src/main/resources/
│   └── application-docker.yaml          ← Configuration Spring pour Docker
├── .dockerignore                        ← Fichiers à ignorer dans l'image
├── public_key.pem                       ← Clé JWT (copier si absent)
└── logs/                                ← Volume pour les logs
```

---

## 🏗️ Architecture Docker

```
┌─────────────────────────────────────────────────────────┐
│         Docker Compose Network                          │
│      (admin-moderation-network - bridge)                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────┐                                  │
│  │  PostgreSQL      │                                  │
│  │  Port: 5432      │                                  │
│  │  User: admin     │                                  │
│  │  Pass: admin123  │                                  │
│  │  DB: admin_db    │                                  │
│  └──────────────────┘                                  │
│           ▲                                             │
│           │                                             │
│  ┌────────┴──────────────────────────────────────┐    │
│  │                                                 │    │
│  │   ┌──────────────────────────────────────┐    │    │
│  │   │  Zookeeper (Port 2181)                │    │    │
│  │   └──────────────────────────────────────┘    │    │
│  │                  ▲                              │    │
│  │                  │                              │    │
│  │   ┌──────────────┴────────────────────────┐   │    │
│  │   │  Kafka (Port 9092/29092)               │   │    │
│  │   │  - Topics: auto-created                │   │    │
│  │   │  - Replication factor: 1               │   │    │
│  │   └──────────────────────────────────────┘   │    │
│  │                  ▲                              │    │
│  │                  │                              │    │
│  │   ┌──────────────┴────────────────────────┐   │    │
│  │   │  Admin Moderation Service (8090)      │   │    │
│  │   │  - Liquibase migration auto           │   │    │
│  │   │  - JPA/Hibernate activate             │   │    │
│  │   │  - WebClient x2 (vendor, product)    │   │    │
│  │   │  - Kafka consumers enabled            │   │    │
│  │   └──────────────────────────────────────┘   │    │
│  │                                                │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Étape 1 : Préparation

### 1.1 Vérifier les prérequis

```bash
# Docker et Docker Compose
docker --version
docker-compose --version

# Résultat attendu:
# Docker version 20.10+ 
# Docker Compose version 2.0+
```

### 1.2 Vérifier la présence de public_key.pem

```bash
# À la racine du projet
ls -la public_key.pem

# Si absent, le créer (JWT dev key)
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

### 1.3 Vérifier les ports disponibles

```bash
# Ports requis:
# - 5432 (PostgreSQL)
# - 2181 (Zookeeper)
# - 9092 (Kafka - intra-network)
# - 29092 (Kafka - localhost)
# - 8090 (Admin Moderation Service)

# Vérifier sur Windows PowerShell
netstat -ano | findstr "5432\|2181\|9092\|29092\|8090"

# Sur Linux/Mac
lsof -i :5432,2181,9092,29092,8090
```

---

## 🔧 Étape 2 : Déploiement

### 2.1 Build et démarrage complet (RECOMMENDED)

```bash
# Approche recommandée: tout construire et démarrer en une commande
cd admin-moderation-service

docker-compose up --build

# Résultat attendu:
# - PostgreSQL: "HikariPool-1 - Start completed."
# - Zookeeper: "binding to port 0.0.0.0/0.0.0.0:2181"
# - Kafka: "started (kafka.server.KafkaServer)"
# - Admin Service: "Started AdminServiceApplication in X seconds"
```

**IMPORTANT:** Ne pas fermer cette fenêtre ! Vous verrez tous les logs en temps réel.

### 2.2 Démarrage en arrière-plan

```bash
# Si vous préférez laisser les services en arrière-plan:
docker-compose up --build -d

# Vérifier l'état des services
docker-compose ps

# Résultat attendu:
# CONTAINER ID   IMAGE                    COMMAND                 STATUS
# xxxxxx         postgres:16-alpine       "docker-entrypoint..."  Up 2 minutes (healthy)
# xxxxxx         confluentinc/cp-zook...  "/etc/confluent/..."   Up 2 minutes (healthy)
# xxxxxx         confluentinc/cp-kafka:7  "/etc/confluent/..."   Up 2 minutes (healthy)
# xxxxxx         admin-moderation-svc     "java -jar app.jar"     Up 1 minute (healthy)
```

### 2.3 Construire uniquement (sans démarrer)

```bash
docker-compose build

# Cela va:
# - Construire l'image du service (multi-stage)
# - Compiler avec Maven Wrapper
# - Créer l'image finale ~600MB
```

### 2.4 Démarrer sans rebuild

```bash
docker-compose up -d

# Lance les services existants sans recompilation
```

---

## 📊 Étape 3 : Vérification et Tests

### 3.1 Vérifier la santé des services

```bash
# Health check via Docker
docker-compose ps

# Vérifier chaque service:

# PostgreSQL
docker-compose exec postgres pg_isready -U admin

# Zookeeper
docker-compose exec zookeeper echo ruok | nc localhost 2181

# Kafka
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Admin Service
curl -f http://localhost:8090/actuator/health
```

### 3.2 Voir les logs en temps réel

```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f admin-moderation-service
docker-compose logs -f postgres
docker-compose logs -f kafka

# Dernières N lignes
docker-compose logs --tail=100 admin-moderation-service
```

### 3.3 Tester les endpoints

```bash
# Health check (publique, pas d'authentification)
curl http://localhost:8090/actuator/health
# Résultat: {"status":"UP"}

# Swagger/OpenAPI (si activé)
# http://localhost:8090/swagger-ui.html

# Metrics
curl http://localhost:8090/actuator/metrics
```

### 3.4 Vérifier Liquibase

```bash
# Logs de migration Liquibase
docker-compose logs admin-moderation-service | grep -i liquibase

# Résultat attendu:
# liquibase.changelog: Reading from public.databasechangelog
# liquibase.ui: Database is up to date, no changesets to execute
# liquibase.util: Total change sets: 4
```

### 3.5 Vérifier la base PostgreSQL

```bash
# Connexion à PostgreSQL
docker-compose exec postgres psql -U admin -d admin_db

# Dans psql:
postgres=# \dt
# Affiche toutes les tables créées par Liquibase

postgres=# SELECT * FROM databasechangelog;
# Affiche l'historique des migrations

postgres=# \q
# Quitter
```

### 3.6 Tester Kafka

```bash
# Lister les topics
docker-compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Créer un topic de test
docker-compose exec kafka kafka-topics.sh \
  --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1

# Voir les topics de l'app
docker-compose exec kafka kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092 | grep -E "price|vendor|user|moderation"
```

---

## 🛑 Étape 4 : Arrêt et Nettoyage

### 4.1 Arrêt gracieux

```bash
# Arrêter les services (volumes persistent)
docker-compose down

# Arrêter avec suppression des volumes
docker-compose down -v

# Arrêter une seule service
docker-compose stop admin-moderation-service
docker-compose start admin-moderation-service
```

### 4.2 Redémarrage

```bash
# Redémarrer tous les services
docker-compose restart

# Redémarrer un service spécifique
docker-compose restart admin-moderation-service
```

### 4.3 Nettoyer complètement

```bash
# Supprimer tout (images, containers, volumes, networks)
docker-compose down -v --remove-orphans

# Reconstruire de zéro
docker system prune -a
docker-compose up --build -d
```

---

## 🐛 Dépannage

### Problème: "Port X is already allocated"

```bash
# Trouver le process qui utilise le port
netstat -ano | findstr :8090  # Windows
lsof -i :8090                 # Linux/Mac

# Tuer le process
taskkill /PID <PID> /F        # Windows
kill -9 <PID>                 # Linux/Mac

# Ou changer les ports dans docker-compose.yaml
# ports:
#   - "8091:8090"   # Nouvel externe -> 8090 interne
```

### Problème: "PostgreSQL connection refused"

```bash
# Vérifier que postgres est healthy
docker-compose ps postgres

# Si DOWN, redémarrer
docker-compose restart postgres

# Voir les logs
docker-compose logs postgres

# Réinitialiser PostgreSQL
docker-compose down -v postgres
docker-compose up postgres -d
```

### Problème: "Liquibase lock timeout"

```bash
# Supprimer le lock Liquibase
docker-compose exec postgres psql -U admin -d admin_db -c \
  "UPDATE databasechangeloglock SET locked = false;"

# Redémarrer le service
docker-compose restart admin-moderation-service
```

### Problème: "Kafka connection timeout"

```bash
# Vérifier que Kafka est démarré
docker-compose ps kafka zookeeper

# Vérifier la connectivité
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server kafka:9092

# Vérifier le bootstrap-servers dans application-docker.yaml
# Doit être: kafka:9092 (pas localhost:9092)
```

### Problème: "Application startup failed"

```bash
# Lire les logs complets
docker-compose logs -f admin-moderation-service

# Chercher les erreurs:
# - QueryCreationException: repository method mismatch
# - UnsatisfiedDependencyException: bean not found
# - ClassNotFoundException: missing dependency
# - DataAccessResourceFailureException: DB connection error

# Exemples de fix:
# 1. Vérifier application-docker.yaml datasource URL
# 2. Vérifier que PostgreSQL est UP (docker-compose logs postgres)
# 3. Vérifier les imports Maven dans pom.xml
```

### Problème: "Out of Memory"

```bash
# Augmenter la mémoire JVM dans Dockerfile
# ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]

# Ou via docker-compose
environment:
  _JAVA_OPTIONS: "-Xmx512m -Xms256m"
```

---

## 📈 Monitoring

### Via Docker Stats

```bash
# Voir la consommation de ressources en temps réel
docker stats admin-moderation-service postgres kafka zookeeper

# Affichage:
# CONTAINER             CPU %   MEM USAGE / LIMIT
# admin-moderation-svc  0.5%    450MB / 2GB
# postgres              1.2%    80MB / 2GB
# kafka                 2.1%    550MB / 2GB
# zookeeper             0.8%    120MB / 2GB
```

### Via Spring Actuator

```bash
# Liveness probe (est-ce que l'app tourne?)
curl http://localhost:8090/actuator/health/liveness
# {"status":"UP"}

# Readiness probe (est-ce que l'app est prête?)
curl http://localhost:8090/actuator/health/readiness
# {"status":"UP","components":{...}}

# Détails complets
curl http://localhost:8090/actuator/health \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔐 Sécurité

### Points importants

1. **Non-root user**: L'app tourne avec l'utilisateur `appuser` (pas root)
2. **Secrets**: Utiliser Docker secrets ou variables d'env en production
3. **JWT public_key.pem**: Monté en read-only dans le container
4. **Network**: Bridge network privé, pas d'exposition directe
5. **Healthcheck**: Liveness & Readiness probes activées

### Pour la production

```yaml
# Utiliser des secrets Docker Compose
secrets:
  db_password:
    file: ./secrets/db_password.txt

services:
  postgres:
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password

# Ou utiliser des variables d'env externes
env_file:
  - .env.production
```

---

## 📝 Commandes Résumé

```bash
# Build tout
docker-compose build

# Démarrer tout en logs
docker-compose up --build

# Démarrer en arrière-plan
docker-compose up --build -d

# Arrêter
docker-compose down

# Arrêter et supprimer volumes
docker-compose down -v

# Logs en temps réel
docker-compose logs -f

# Logs du service uniquement
docker-compose logs -f admin-moderation-service

# Redémarrer un service
docker-compose restart admin-moderation-service

# Shell dans un container
docker-compose exec admin-moderation-service /bin/bash
docker-compose exec postgres psql -U admin -d admin_db

# Stats
docker stats

# Vérifier la santé
curl http://localhost:8090/actuator/health
```

---

## ✅ Checklist de déploiement

- [ ] `public_key.pem` présent à la racine
- [ ] Ports 5432, 2181, 9092, 29092, 8090 disponibles
- [ ] Docker et Docker Compose >= 2.0
- [ ] Fichiers présents:
  - [ ] `Dockerfile`
  - [ ] `docker-compose.yaml`
  - [ ] `src/main/resources/application-docker.yaml`
- [ ] `mvnw` exécutable: `chmod +x mvnw`
- [ ] `pom.xml` avec dépendances correctes
- [ ] `src/main/resources/db/changelog/db.changelog-master.yaml` existe
- [ ] Pas d'erreurs de compilation locale (`mvn clean compile`)

---

## 🎯 Au lancement final

```bash
# À la racine du projet
docker-compose up --build

# Attendez ces messages dans les logs:
# 
# [INFO] admin-moderation-postgres | creating "admin" user
# [INFO] admin-moderation-postgres | PostgreSQL Database System is ready
# [INFO] admin-moderation-zookeeper | binding to port 0.0.0.0/0.0.0.0:2181
# [INFO] admin-moderation-kafka | started (kafka.server.KafkaServer)
# [INFO] admin-moderation-service | Started AdminServiceApplication in X.XXX seconds
#
# ✅ DONE! Service running on http://localhost:8090
```

---

**Document généré:** 2025-12-08  
**Version:** 1.0  
**Status:** Production Ready ✅

