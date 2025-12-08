# 🚀 LIQUIBASE DEPLOYMENT GUIDE - Admin Moderation Service

## 📋 Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Structure des fichiers](#structure-des-fichiers)
3. [Configuration](#configuration)
4. [Déploiement](#déploiement)
5. [Vérification](#vérification)
6. [Troubleshooting](#troubleshooting)
7. [Maintenance](#maintenance)

---

## Vue d'ensemble

### Qu'est-ce que Liquibase ?
Liquibase est un outil de gestion des versions pour les bases de données. Il permet de :
- **Versionner** les changements de schéma (comme Git pour la BD)
- **Automatiser** les migrations au démarrage de l'application
- **Tracer** l'historique des modifications
- **Assurer l'idempotence** (pas de doublons d'exécution)

### Objectif
Créer 4 tables PostgreSQL pour le microservice Admin Moderation :
- `admin_action_log` → Logging des actions
- `vendor_status_history` → Historique des changements
- `moderation_review` → Revues de modération
- `admin_stats_cache` → Cache des statistiques

---

## Structure des fichiers

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml          ← FICHIER MAÎTRE
        ├── tables/
        │   ├── V1_admin_action_log.yaml      ← V1: Table de logging
        │   ├── V2_vendor_status_history.yaml ← V2: Historique statuts
        │   ├── V3_moderation_review.yaml     ← V3: Revues
        │   └── V4_admin_stats_cache.yaml     ← V4: Cache stats
        ├── changelog-master.xml              ← ANCIEN (XML)
        └── create-tables.xml                 ← ANCIEN (XML)
```

**Architecture** :
- Le fichier **maître** (`db.changelog-master.yaml`) inclut tous les autres fichiers
- Chaque table a son propre fichier YAML versionné
- Versioning : **V1, V2, V3, V4** pour clarté

---

## Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://ep-purple-violet-agbhwbie-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require
    username: neondb_owner
    password: npg_9zLEfBC3ZlDs
    driver-class-name: org.postgresql.Driver

  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    default-schema: public
    drop-first: false

  jpa:
    hibernate:
      ddl-auto: none  # ⚠️ CRUCIAL: Laisser Liquibase gérer
```

**Paramètres critiques** :
| Paramètre | Valeur | Raison |
|-----------|--------|--------|
| `enabled` | `true` | Activation de Liquibase |
| `change-log` | `classpath:db/changelog/db.changelog-master.yaml` | Chemin du fichier maître |
| `default-schema` | `public` | Schéma PostgreSQL |
| `drop-first` | `false` | Ne pas supprimer les données |
| `ddl-auto` | `none` | Hibernate ne gère PAS les schémas |

---

## Déploiement

### Option 1 : Déploiement Local (Développement)

#### Prérequis
- Java 21+
- PostgreSQL ou Neon (instance actuelle)
- Maven 3.8+

#### Étapes
```bash
# 1. Cloner/Naviguer vers le projet
cd admin-moderation-service

# 2. Compiler
mvn clean compile

# 3. Exécuter les tests (optionnel)
mvn test

# 4. Démarrer l'application (les migrations s'exécutent automatiquement)
mvn spring-boot:run

# 5. Vérifier dans les logs
# [INFO] Initializing Liquibase database
# [INFO] Executing changeset: V1_create_admin_action_log_table
# [INFO] Executing changeset: V2_create_vendor_status_history_table
# [INFO] Executing changeset: V3_create_moderation_review_table
# [INFO] Executing changeset: V4_create_admin_stats_cache_table
```

### Option 2 : Déploiement Docker

#### Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY target/admin-moderation-service-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Build & Run
```bash
# Build
mvn clean package -DskipTests
docker build -t admin-moderation-service:1.0.0 .

# Run
docker run -e SPRING_DATASOURCE_URL=... \
           -e SPRING_DATASOURCE_USERNAME=... \
           -e SPRING_DATASOURCE_PASSWORD=... \
           -p 8090:8090 \
           admin-moderation-service:1.0.0
```

### Option 3 : Déploiement Kubernetes

#### deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: admin-moderation-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: admin-moderation-service
        image: admin-moderation-service:1.0.0
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: db-url
        - name: LIQUIBASE_ENABLED
          value: "true"
        ports:
        - containerPort: 8090
```

### Option 4 : Déploiement Cloud (Neon/AWS)

#### Étapes
```bash
# 1. Build JAR
mvn clean package -DskipTests

# 2. Push vers artifact registry (Docker)
docker tag admin-moderation-service:1.0.0 <registry>/admin-moderation-service:1.0.0
docker push <registry>/admin-moderation-service:1.0.0

# 3. Configurer les variables d'environnement
export SPRING_DATASOURCE_URL=jdbc:postgresql://neon-instance/neondb
export SPRING_DATASOURCE_USERNAME=neondb_owner
export SPRING_DATASOURCE_PASSWORD=<password>

# 4. Déployer sur Cloud Run / ECS / Lambda
# (Utiliser votre plate-forme de choix)
```

---

## Vérification

### 1. Vérifier les logs au démarrage
```bash
# Rechercher les lignes Liquibase
grep -i "liquibase\|changeset" app.log

# Résultat attendu:
# [INFO] Initializing Liquibase database
# [INFO] Executing changeset: V1_create_admin_action_log_table
# [INFO] Executing changeset: V2_create_vendor_status_history_table
# [INFO] Executing changeset: V3_create_moderation_review_table
# [INFO] Executing changeset: V4_create_admin_stats_cache_table
```

### 2. Vérifier les tables créées (via psql ou pgAdmin)
```sql
-- Afficher toutes les tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Résultat attendu:
-- admin_action_log
-- admin_stats_cache
-- databasechangelog
-- databasechangeloglock
-- moderation_review
-- vendor_status_history
```

### 3. Vérifier les migrations enregistrées
```sql
-- Afficher l'historique des migrations
SELECT id, author, filename, dateexecuted, exectype 
FROM databasechangelog 
ORDER BY dateexecuted DESC;

-- Résultat attendu: 4 rows
-- V1_create_admin_action_log_table
-- V2_create_vendor_status_history_table
-- V3_create_moderation_review_table
-- V4_create_admin_stats_cache_table
```

### 4. Vérifier les indexes
```sql
-- Afficher tous les indexes
SELECT indexname FROM pg_indexes 
WHERE schemaname = 'public' 
ORDER BY indexname;

-- Résultat attendu: 6 indexes
-- idx_admin_action_log_action_type
-- idx_admin_action_log_admin_id
-- idx_admin_stats_metric_name
-- idx_moderation_review_report_id
-- idx_vendor_status_vendor_id
-- (+ primary keys)
```

### 5. Exécuter le script de vérification complet
```bash
# Exécuter le script SQL de test
psql -h <host> -U <user> -d neondb -f LIQUIBASE_VERIFICATION.sql
```

---

## Troubleshooting

### Problème 1 : "Liquibase is locked"
```
ERROR: Liquibase is locked. See databasechangeloglock table
```

**Solution** :
```sql
-- Vérifier le statut
SELECT * FROM databasechangeloglock;

-- Déverrouiller (si nécessaire)
UPDATE databasechangeloglock SET locked = false;
```

### Problème 2 : "File not found: db/changelog/db.changelog-master.yaml"
**Solution** :
- Vérifier que le fichier existe dans `src/main/resources/db/changelog/`
- Vérifier l'indentation du chemin dans `application.yml`
- Nettoyer et recompiler : `mvn clean compile`

### Problème 3 : "PostgreSQL connection refused"
**Solution** :
- Vérifier les credentials dans `application.yml`
- Tester la connexion : `psql -h <host> -U <user> -d neondb`
- Vérifier le SSL mode : `?sslmode=require`

### Problème 4 : "Duplicate key value violates unique constraint"
```
ERROR: duplicate key value violates unique constraint "idx_admin_stats_metric_name"
```

**Solution** :
```sql
-- Vérifier les doublons
SELECT metric_name, COUNT(*) FROM admin_stats_cache GROUP BY metric_name HAVING COUNT(*) > 1;

-- Nettoyer si nécessaire
DELETE FROM admin_stats_cache WHERE metric_name = 'duplicate_metric' AND id NOT IN (
  SELECT MIN(id) FROM admin_stats_cache GROUP BY metric_name
);
```

### Problème 5 : Liquibase ne s'exécute pas au démarrage
**Vérifier** :
- `spring.liquibase.enabled=true` dans `application.yml`
- Pas d'erreur dans les logs (vérifier DEBUG level)
- Base de données accessible
- Fichiers YAML correctement indentés (2 espaces)

---

## Maintenance

### Ajouter une nouvelle table (V5)

#### 1. Créer le fichier
`src/main/resources/db/changelog/tables/V5_new_table.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: V5_create_new_table
      author: admin-service
      logicalFilePath: db/changelog/tables/V5_new_table.yaml
      changes:
        - createTable:
            tableName: new_table
            schemaName: public
            columns:
              - column:
                  name: id
                  type: bigint
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              # ... autres colonnes
```

#### 2. Ajouter au fichier maître
`db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  include:
    - file: db/changelog/tables/V1_admin_action_log.yaml
    - file: db/changelog/tables/V2_vendor_status_history.yaml
    - file: db/changelog/tables/V3_moderation_review.yaml
    - file: db/changelog/tables/V4_admin_stats_cache.yaml
    - file: db/changelog/tables/V5_new_table.yaml  # ← AJOUTER
```

#### 3. Déployer
- Compiler : `mvn clean compile`
- Redémarrer l'application
- Vérifier les logs

### Archivage des données (Best Practice)

```sql
-- Créer table d'archive
CREATE TABLE admin_action_log_archive AS SELECT * FROM admin_action_log 
WHERE created_at < NOW() - INTERVAL '1 year';

-- Supprimer les anciennes données
DELETE FROM admin_action_log WHERE created_at < NOW() - INTERVAL '1 year';

-- Réindexer
REINDEX TABLE admin_action_log;
```

### Monitoring

```sql
-- Taille des tables
SELECT 
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Performance des indexes
SELECT 
  relname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

---

## 📚 Ressources

- **Liquibase Docs** : https://docs.liquibase.com
- **PostgreSQL** : https://www.postgresql.org/docs/current/
- **Spring Data JPA** : https://spring.io/projects/spring-data-jpa
- **Neon Documentation** : https://neon.tech/docs

---

## ✅ Checklist Prédéploiement

- [ ] Configuration PostgreSQL/Neon en place
- [ ] Credentials valides dans `application.yml`
- [ ] Fichiers YAML correctement indentés
- [ ] Compilation réussie : `mvn clean compile`
- [ ] Tests passants : `mvn test`
- [ ] Package généré : `mvn package -DskipTests`
- [ ] Backups créés (si migration existante)
- [ ] Équipe informée du déploiement
- [ ] Plan de rollback préparé

---

## 🚀 Go-Live

```bash
# 1. Dernière compilation
mvn clean package -DskipTests

# 2. Déployer (Docker/K8s/Cloud)
docker run -e SPRING_DATASOURCE_URL=... admin-moderation-service:1.0.0

# 3. Vérifier les logs
# [INFO] Initializing Liquibase database... ✅

# 4. Tester les endpoints
curl http://localhost:8090/admin/products

# 5. Monitorer les métriques
# /actuator/health → UP ✅
# /actuator/metrics → Actif ✅

# ✅ DEPLOYED SUCCESSFULLY!
```

---

**Système Liquibase opérationnel et prêt pour la production ! 🚀**

*Dernière mise à jour : 2025-12-08*
*Author : Admin Moderation Service Team*
