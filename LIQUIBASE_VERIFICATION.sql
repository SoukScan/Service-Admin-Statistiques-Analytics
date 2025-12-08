-- ============================================================================
-- ADMIN MODERATION SERVICE - LIQUIBASE VERIFICATION SCRIPT
-- ============================================================================
-- Exécutez ce script après le déploiement pour valider l'intégrité complète

-- ============================================================================
-- 1. VÉRIFIER LA STRUCTURE DES TABLES
-- ============================================================================

-- Afficher toutes les tables créées par Liquibase
\echo '=== TABLES CRÉÉES ==='
SELECT 
  t.table_name,
  t.table_type,
  pg_size_pretty(pg_total_relation_size('public.' || t.table_name)) as table_size,
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE t.table_schema = 'public' AND t.table_type = 'BASE TABLE'
ORDER BY t.table_name;

-- ============================================================================
-- 2. VÉRIFIER LES COLONNES DE CHAQUE TABLE
-- ============================================================================

\echo '=== COLONNES DE admin_action_log ==='
SELECT 
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'admin_action_log' AND table_schema = 'public'
ORDER BY ordinal_position;

\echo '=== COLONNES DE vendor_status_history ==='
SELECT 
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'vendor_status_history' AND table_schema = 'public'
ORDER BY ordinal_position;

\echo '=== COLONNES DE moderation_review ==='
SELECT 
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'moderation_review' AND table_schema = 'public'
ORDER BY ordinal_position;

\echo '=== COLONNES DE admin_stats_cache ==='
SELECT 
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'admin_stats_cache' AND table_schema = 'public'
ORDER BY ordinal_position;

-- ============================================================================
-- 3. VÉRIFIER LES INDEXES
-- ============================================================================

\echo '=== INDEXES CRÉÉS ==='
SELECT 
  schemaname,
  tablename,
  indexname,
  indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- ============================================================================
-- 4. VÉRIFIER LES CONSTRAINTS
-- ============================================================================

\echo '=== CONSTRAINTS PRIMAIRES ==='
SELECT 
  constraint_name,
  table_name,
  constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'public' AND constraint_type = 'PRIMARY KEY'
ORDER BY table_name;

\echo '=== CONSTRAINTS UNIQUES ==='
SELECT 
  constraint_name,
  table_name,
  constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'public' AND constraint_type = 'UNIQUE'
ORDER BY table_name;

-- ============================================================================
-- 5. VÉRIFIER L'HISTORIQUE LIQUIBASE
-- ============================================================================

\echo '=== MIGRATIONS EXÉCUTÉES ==='
SELECT 
  id,
  author,
  filename,
  TO_CHAR(dateexecuted, 'YYYY-MM-DD HH24:MI:SS') as execution_time,
  exectype,
  description
FROM databasechangelog
WHERE filename LIKE 'db/changelog%'
ORDER BY dateexecuted ASC;

\echo '=== RÉSUMÉ LIQUIBASE ==='
SELECT 
  COUNT(*) as total_migrations,
  COUNT(CASE WHEN exectype = 'EXECUTED' THEN 1 END) as executed,
  COUNT(CASE WHEN exectype = 'MARK_EXECUTED' THEN 1 END) as marked,
  MAX(dateexecuted) as latest_execution
FROM databasechangelog;

-- ============================================================================
-- 6. TESTS D'INTÉGRITÉ
-- ============================================================================

\echo '=== TEST 1: INSÉRER UN ENREGISTREMENT DANS admin_action_log ==='
BEGIN;
INSERT INTO public.admin_action_log 
(admin_id, action_type, target_type, target_id, description) 
VALUES (1, 'TEST', 'PRODUCT', 999, 'Test d''intégrité');
SELECT 'Insertion réussie' as status;
ROLLBACK;

\echo '=== TEST 2: VÉRIFIER LE TIMESTAMP AUTOMATIQUE ==='
BEGIN;
INSERT INTO public.vendor_status_history 
(vendor_id, new_status) 
VALUES (1, 'ACTIVE');
SELECT 
  created_at::timestamp > NOW() - INTERVAL '10 seconds' as timestamp_ok,
  created_at::timestamp <= NOW() as is_current
FROM public.vendor_status_history 
WHERE vendor_id = 1 
ORDER BY created_at DESC LIMIT 1;
ROLLBACK;

\echo '=== TEST 3: VÉRIFIER L''UNICITÉ DE metric_name ==='
BEGIN;
INSERT INTO public.admin_stats_cache (metric_name, metric_value) VALUES ('test_metric', 100);
INSERT INTO public.admin_stats_cache (metric_name, metric_value) VALUES ('test_metric', 200);
ROLLBACK;

\echo '=== TEST 4: VÉRIFIER LES INDEX ==='
SELECT 
  schemaname,
  tablename,
  indexname,
  idx_scan as index_scans,
  idx_tup_read as tuples_read,
  idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY tablename;

-- ============================================================================
-- 7. VÉRIFIER LES SÉQUENCES AUTO_INCREMENT
-- ============================================================================

\echo '=== SÉQUENCES ==='
SELECT 
  sequence_schema,
  sequence_name,
  start_value,
  min_value,
  max_value,
  increment,
  cycle
FROM information_schema.sequences
WHERE sequence_schema = 'public'
ORDER BY sequence_name;

-- ============================================================================
-- 8. RAPPORT FINAL
-- ============================================================================

\echo '=== RAPPORT DE VÉRIFICATION FINAL ==='
WITH table_checks AS (
  SELECT 
    'admin_action_log' as table_name,
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'admin_action_log' AND table_schema = 'public') as exists_check,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'admin_action_log') = 7 as columns_ok,
    EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'admin_action_log' AND indexname = 'idx_admin_action_log_admin_id') as idx1_ok,
    EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'admin_action_log' AND indexname = 'idx_admin_action_log_action_type') as idx2_ok
  UNION ALL
  SELECT 
    'vendor_status_history',
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'vendor_status_history' AND table_schema = 'public'),
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'vendor_status_history') = 6,
    EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'vendor_status_history' AND indexname = 'idx_vendor_status_vendor_id'),
    TRUE
  UNION ALL
  SELECT 
    'moderation_review',
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'moderation_review' AND table_schema = 'public'),
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'moderation_review') = 6,
    EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'moderation_review' AND indexname = 'idx_moderation_review_report_id'),
    TRUE
  UNION ALL
  SELECT 
    'admin_stats_cache',
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'admin_stats_cache' AND table_schema = 'public'),
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'admin_stats_cache') = 4,
    EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'admin_stats_cache' AND indexname = 'idx_admin_stats_metric_name'),
    TRUE
)
SELECT 
  table_name,
  CASE WHEN exists_check THEN '✅' ELSE '❌' END as table_exists,
  CASE WHEN columns_ok THEN '✅' ELSE '❌' END as columns_correct,
  CASE WHEN idx1_ok THEN '✅' ELSE '❌' END as index1_ok,
  CASE WHEN idx2_ok THEN '✅' ELSE '❌' END as index2_ok,
  CASE WHEN exists_check AND columns_ok AND idx1_ok AND idx2_ok THEN '✅ PASS' ELSE '❌ FAIL' END as overall
FROM table_checks
ORDER BY table_name;

-- ============================================================================
-- FIN DU SCRIPT DE VÉRIFICATION
-- ============================================================================

\echo '✅ Vérification complète terminée!'
