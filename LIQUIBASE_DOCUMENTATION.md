================================================================================
  ADMIN MODERATION SERVICE - SYSTÈME LIQUIBASE COMPLET
================================================================================

📊 STRUCTURE DES TABLES
================================================================================

MODULE 1 : ADMIN ACTION LOGGING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Table: admin_action_log
├─ id (PK, BIGINT AUTO_INCREMENT)
├─ admin_id (BIGINT NOT NULL) → Actions des administrateurs
├─ action_type (VARCHAR(100)) → Type d'action (CREATE, UPDATE, DELETE, etc.)
├─ target_type (VARCHAR(100)) → Type de cible (VENDOR, PRODUCT, USER, etc.)
├─ target_id (BIGINT) → ID de la ressource affectée
├─ description (TEXT) → Détails de l'action
└─ created_at (TIMESTAMP DEFAULT NOW())

Indexes:
  ✓ idx_admin_action_log_admin_id → Recherche rapide par admin
  ✓ idx_admin_action_log_action_type → Recherche rapide par type d'action

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


MODULE 2 : VENDOR STATUS HISTORY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Table: vendor_status_history
├─ id (PK, BIGINT AUTO_INCREMENT)
├─ vendor_id (BIGINT NOT NULL) → Référence au vendeur
├─ old_status (VARCHAR(100)) → Ancien statut (ACTIVE, INACTIVE, SUSPENDED, etc.)
├─ new_status (VARCHAR(100) NOT NULL) → Nouveau statut
├─ changed_at (TIMESTAMP DEFAULT NOW()) → Date du changement
└─ changed_by_admin (BIGINT) → Admin qui a effectué le changement

Indexes:
  ✓ idx_vendor_status_vendor_id → Historique rapide par vendeur

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


MODULE 3 : MODERATION REVIEW
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Table: moderation_review
├─ id (PK, BIGINT AUTO_INCREMENT)
├─ report_id (BIGINT NOT NULL) → Référence au signalement
├─ reviewer_admin_id (BIGINT NOT NULL) → Admin qui a examiné le signalement
├─ decision (VARCHAR(100) NOT NULL) → Décision (APPROVED, REJECTED, PENDING, etc.)
├─ notes (TEXT) → Notes de la revue
└─ reviewed_at (TIMESTAMP DEFAULT NOW())

Indexes:
  ✓ idx_moderation_review_report_id → Recherche rapide par signalement

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


MODULE 4 : ADMIN STATS CACHE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Table: admin_stats_cache
├─ id (PK, BIGINT AUTO_INCREMENT)
├─ metric_name (VARCHAR(255) UNIQUE NOT NULL) → Nom de la métrique
├─ metric_value (BIGINT) → Valeur numérique de la métrique
└─ updated_at (TIMESTAMP DEFAULT NOW())

Indexes:
  ✓ idx_admin_stats_metric_name → Recherche rapide par métrique (UNIQUE)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


📊 DIAGRAMME ERD (ENTITY RELATIONSHIP DIAGRAM)
================================================================================

                        ┌─────────────────────────┐
                        │   admin_action_log      │
                        ├─────────────────────────┤
                        │ id (PK)                 │
                        │ admin_id (FK)           │
                        │ action_type             │
                        │ target_type             │
                        │ target_id               │
                        │ description             │
                        │ created_at              │
                        └─────────────────────────┘
                                ▲
                                │ admin_id references
                                │ (external: Admin Service)


   ┌──────────────────────────┐         ┌──────────────────────────────┐
   │ vendor_status_history    │         │   moderation_review          │
   ├──────────────────────────┤         ├──────────────────────────────┤
   │ id (PK)                  │         │ id (PK)                      │
   │ vendor_id (FK)           │         │ report_id (FK)               │
   │ old_status               │         │ reviewer_admin_id (FK)       │
   │ new_status               │         │ decision                     │
   │ changed_at               │         │ notes                        │
   │ changed_by_admin (FK)    │         │ reviewed_at                  │
   └──────────────────────────┘         └──────────────────────────────┘
           ▲                                        ▲
           │ vendor_id references                  │ report_id references
           │ (external: Vendor Service)            │ (external: Report/Moderation Service)
           │                                       │
           │ changed_by_admin references          │ reviewer_admin_id references
           │ (external: Admin Service)             │ (external: Admin Service)


                        ┌─────────────────────────┐
                        │  admin_stats_cache      │
                        ├─────────────────────────┤
                        │ id (PK)                 │
                        │ metric_name (UNIQUE)    │
                        │ metric_value            │
                        │ updated_at              │
                        └─────────────────────────┘


📁 STRUCTURE COMPLÈTE DES FICHIERS LIQUIBASE
================================================================================

src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml (FICHIER MAÎTRE)
        └── tables/
            ├── V1_admin_action_log.yaml
            ├── V2_vendor_status_history.yaml
            ├── V3_moderation_review.yaml
            └── V4_admin_stats_cache.yaml


🔧 CONFIGURATION APPLICATION.YML
================================================================================

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
      ddl-auto: none
    show-sql: false


📋 VERSIONING LIQUIBASE
================================================================================

✓ V1 : admin_action_log (ID: V1_create_admin_action_log_table)
✓ V2 : vendor_status_history (ID: V2_create_vendor_status_history_table)
✓ V3 : moderation_review (ID: V3_create_moderation_review_table)
✓ V4 : admin_stats_cache (ID: V4_create_admin_stats_cache_table)


✅ AVANTAGES DE CE SYSTÈME LIQUIBASE
================================================================================

1. ✓ Versioning clair avec les fichiers V1, V2, V3, V4
2. ✓ Séparation logique des tables dans des fichiers distincts
3. ✓ Inclusions gérées par un fichier maître centralisé
4. ✓ Compatible avec PostgreSQL/Neon
5. ✓ Schéma public explicitement défini
6. ✓ Indexes configurés pour performances optimales
7. ✓ Timestamps configurés avec DEFAULT NOW()
8. ✓ YAML correctement indenté et lisible
9. ✓ Facilement extensible pour futures migrations
10. ✓ Intégration transparente avec Spring Boot JPA


🚀 DÉPLOIEMENT
================================================================================

1. Application au démarrage :
   → Liquibase vérifie la base de données
   → Les tables manquantes sont créées automatiquement
   → Les indexes sont créés pour performances optimales

2. Logs Liquibase attendus au démarrage :
   [INFO] Initializing Liquibase database
   [INFO] Reading resource: db/changelog/db.changelog-master.yaml
   [INFO] Executing changeset: V1_create_admin_action_log_table
   [INFO] Executing changeset: V2_create_vendor_status_history_table
   [INFO] Executing changeset: V3_create_moderation_review_table
   [INFO] Executing changeset: V4_create_admin_stats_cache_table


================================================================================
                        SYSTÈME LIQUIBASE OPÉRATIONNEL ✅
================================================================================
