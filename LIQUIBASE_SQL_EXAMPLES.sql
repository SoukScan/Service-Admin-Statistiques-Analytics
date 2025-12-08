-- ============================================================================
-- ADMIN MODERATION SERVICE - SQL EXAMPLES & QUERIES
-- ============================================================================
-- Ce fichier contient des exemples pratiques d'utilisation des tables Liquibase

-- ============================================================================
-- MODULE 1 : ADMIN ACTION LOG
-- ============================================================================

-- Exemple 1 : Enregistrer une action d'un admin
INSERT INTO public.admin_action_log 
(admin_id, action_type, target_type, target_id, description) 
VALUES 
(1, 'SUSPEND', 'VENDOR', 456, 'Suspension du vendeur pour violation des CGU'),
(1, 'DELETE', 'PRODUCT', 789, 'Suppression du produit contrefait'),
(2, 'UPDATE', 'VENDOR', 456, 'Mise à jour du statut de vérification'),
(2, 'CREATE', 'REPORT', 100, 'Création d''un signalement manuel');

-- Exemple 2 : Chercher toutes les actions d'un admin
SELECT 
  id, 
  action_type, 
  target_type, 
  target_id, 
  description, 
  created_at 
FROM public.admin_action_log 
WHERE admin_id = 1 
ORDER BY created_at DESC;

-- Exemple 3 : Compter les actions par type
SELECT 
  action_type, 
  COUNT(*) as total 
FROM public.admin_action_log 
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY action_type 
ORDER BY total DESC;

-- Exemple 4 : Audit trail - Qui a suspendu quel vendeur ?
SELECT 
  aal.id,
  aal.admin_id,
  aal.action_type,
  aal.target_id,
  aal.description,
  aal.created_at
FROM public.admin_action_log aal
WHERE aal.action_type = 'SUSPEND' 
  AND aal.target_type = 'VENDOR'
ORDER BY aal.created_at DESC
LIMIT 10;


-- ============================================================================
-- MODULE 2 : VENDOR STATUS HISTORY
-- ============================================================================

-- Exemple 1 : Enregistrer un changement de statut
INSERT INTO public.vendor_status_history 
(vendor_id, old_status, new_status, changed_by_admin) 
VALUES 
(456, 'PENDING_VERIFICATION', 'ACTIVE', 1),
(789, 'ACTIVE', 'SUSPENDED', 2),
(123, 'SUSPENDED', 'BANNED', 1);

-- Exemple 2 : Historique complet d'un vendeur
SELECT 
  id,
  vendor_id,
  old_status,
  new_status,
  changed_at,
  changed_by_admin
FROM public.vendor_status_history
WHERE vendor_id = 456
ORDER BY changed_at DESC;

-- Exemple 3 : Statut actuel et historique des changements
WITH latest_status AS (
  SELECT 
    vendor_id,
    new_status as current_status,
    changed_at as last_changed,
    changed_by_admin,
    ROW_NUMBER() OVER (PARTITION BY vendor_id ORDER BY changed_at DESC) as rn
  FROM public.vendor_status_history
)
SELECT 
  vendor_id,
  current_status,
  last_changed,
  changed_by_admin
FROM latest_status
WHERE rn = 1
ORDER BY vendor_id;

-- Exemple 4 : Vendeurs ayant eu le plus de changements de statut
SELECT 
  vendor_id,
  COUNT(*) as change_count,
  MAX(changed_at) as last_change
FROM public.vendor_status_history
GROUP BY vendor_id
HAVING COUNT(*) > 1
ORDER BY change_count DESC;

-- Exemple 5 : Combien de temps un vendeur est resté dans chaque statut
SELECT 
  vendor_id,
  new_status,
  changed_at,
  LEAD(changed_at) OVER (PARTITION BY vendor_id ORDER BY changed_at) as next_change,
  EXTRACT(DAY FROM LEAD(changed_at) OVER (PARTITION BY vendor_id ORDER BY changed_at) - changed_at) as days_in_status
FROM public.vendor_status_history
WHERE vendor_id = 456
ORDER BY changed_at DESC;


-- ============================================================================
-- MODULE 3 : MODERATION REVIEW
-- ============================================================================

-- Exemple 1 : Enregistrer une revue de modération
INSERT INTO public.moderation_review 
(report_id, reviewer_admin_id, decision, notes) 
VALUES 
(1001, 1, 'APPROVED', 'Contenu viole clause 3.2 des CGU'),
(1002, 2, 'REJECTED', 'Signalement frauduleux, pas de violation'),
(1003, 1, 'ESCALATED', 'Nécessite investigation juridique'),
(1004, 3, 'APPROVED', 'Produit contrefait confirmé');

-- Exemple 2 : Revues effectuées par un admin
SELECT 
  id,
  report_id,
  decision,
  notes,
  reviewed_at
FROM public.moderation_review
WHERE reviewer_admin_id = 1
ORDER BY reviewed_at DESC;

-- Exemple 3 : Statistiques des décisions
SELECT 
  decision,
  COUNT(*) as total,
  ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) as percentage
FROM public.moderation_review
WHERE reviewed_at >= NOW() - INTERVAL '30 days'
GROUP BY decision
ORDER BY total DESC;

-- Exemple 4 : Temps moyen de revue par admin
SELECT 
  reviewer_admin_id,
  COUNT(*) as reviews_count,
  AVG(EXTRACT(EPOCH FROM (reviewed_at - NOW())) / 3600) as avg_hours_to_review
FROM public.moderation_review
GROUP BY reviewer_admin_id
ORDER BY reviews_count DESC;

-- Exemple 5 : Signalements critiques à escalader
SELECT 
  id,
  report_id,
  reviewer_admin_id,
  decision,
  notes,
  reviewed_at
FROM public.moderation_review
WHERE decision = 'ESCALATED'
  AND reviewed_at >= NOW() - INTERVAL '7 days'
ORDER BY reviewed_at DESC;


-- ============================================================================
-- MODULE 4 : ADMIN STATS CACHE
-- ============================================================================

-- Exemple 1 : Insérer ou mettre à jour des métriques
-- Utiliser UPSERT (ON CONFLICT) pour éviter les doublons
INSERT INTO public.admin_stats_cache 
(metric_name, metric_value) 
VALUES 
('total_vendors_active', 1250),
('total_products_flagged', 342),
('total_reviews_pending', 45),
('avg_moderation_time_seconds', 1847),
('total_admins_online', 8)
ON CONFLICT (metric_name) DO UPDATE SET 
  metric_value = EXCLUDED.metric_value,
  updated_at = NOW();

-- Exemple 2 : Récupérer toutes les métriques actuelles
SELECT 
  metric_name,
  metric_value,
  updated_at
FROM public.admin_stats_cache
ORDER BY metric_name;

-- Exemple 3 : Récupérer une métrique spécifique
SELECT 
  metric_value,
  updated_at
FROM public.admin_stats_cache
WHERE metric_name = 'total_vendors_active';

-- Exemple 4 : Métriques non mises à jour depuis longtemps
SELECT 
  metric_name,
  metric_value,
  updated_at,
  NOW() - updated_at as age
FROM public.admin_stats_cache
WHERE NOW() - updated_at > INTERVAL '1 hour'
ORDER BY age DESC;

-- Exemple 5 : Dashboard - Toutes les métriques avec formatage
SELECT 
  metric_name,
  metric_value,
  TO_CHAR(updated_at, 'YYYY-MM-DD HH24:MI:SS') as last_updated,
  CASE 
    WHEN NOW() - updated_at < INTERVAL '5 minutes' THEN '✅ À jour'
    WHEN NOW() - updated_at < INTERVAL '1 hour' THEN '⚠️ Ancien'
    ELSE '❌ Très ancien'
  END as freshness_status
FROM public.admin_stats_cache
ORDER BY metric_name;


-- ============================================================================
-- CROSS-TABLE QUERIES (ANALYTICS)
-- ============================================================================

-- Exemple 1 : Tableau de bord administrateur complet
SELECT 
  (SELECT COUNT(*) FROM public.admin_action_log WHERE created_at >= NOW() - INTERVAL '1 day') as actions_today,
  (SELECT COUNT(*) FROM public.vendor_status_history WHERE changed_at >= NOW() - INTERVAL '1 day') as status_changes_today,
  (SELECT COUNT(*) FROM public.moderation_review WHERE reviewed_at >= NOW() - INTERVAL '1 day') as reviews_today,
  (SELECT metric_value FROM public.admin_stats_cache WHERE metric_name = 'total_reviews_pending') as pending_reviews;

-- Exemple 2 : Activité des admins
SELECT 
  aal.admin_id,
  COUNT(DISTINCT aal.id) as total_actions,
  COUNT(DISTINCT CASE WHEN aal.created_at >= NOW() - INTERVAL '7 days' THEN aal.id END) as actions_7d,
  COUNT(DISTINCT mr.id) as total_reviews,
  MAX(aal.created_at) as last_action
FROM public.admin_action_log aal
LEFT JOIN public.moderation_review mr ON aal.admin_id = mr.reviewer_admin_id
GROUP BY aal.admin_id
ORDER BY total_actions DESC;

-- Exemple 3 : Rapport de conformité vendeur
SELECT 
  vsh.vendor_id,
  vsh.new_status as current_status,
  vsh.changed_at as status_updated,
  COUNT(mr.id) as total_reviews,
  SUM(CASE WHEN mr.decision = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
  SUM(CASE WHEN mr.decision = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count
FROM public.vendor_status_history vsh
LEFT JOIN public.moderation_review mr ON mr.report_id = vsh.vendor_id
GROUP BY vsh.vendor_id, vsh.new_status, vsh.changed_at
ORDER BY vsh.vendor_id DESC;


-- ============================================================================
-- MAINTENANCE & OPTIMIZATION QUERIES
-- ============================================================================

-- Exemple 1 : Statistiques des tables
SELECT 
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
  n_live_tup as row_count
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Exemple 2 : Vérifier les index
SELECT 
  indexname,
  tablename,
  indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Exemple 3 : Analyser les tables (mise à jour des statistiques)
ANALYZE public.admin_action_log;
ANALYZE public.vendor_status_history;
ANALYZE public.moderation_review;
ANALYZE public.admin_stats_cache;

-- Exemple 4 : Vérifier les séquences
SELECT 
  relname as sequence_name,
  last_value,
  max_value
FROM pg_sequences
WHERE schemaname = 'public'
ORDER BY relname;


-- ============================================================================
-- MIGRATION VERIFICATION QUERIES
-- ============================================================================

-- Vérifier que toutes les tables ont été créées
SELECT 
  table_name,
  table_schema
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Vérifier les colonnes de chaque table
SELECT 
  table_name,
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- Vérifier les constraints
SELECT 
  constraint_name,
  table_name,
  constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'public'
ORDER BY table_name, constraint_name;

-- Vérifier l'historique Liquibase
SELECT 
  id,
  author,
  filename,
  dateexecuted,
  orderexecuted,
  exectype,
  md5sum,
  description
FROM databasechangelog
ORDER BY dateexecuted DESC;

-- ============================================================================
-- END OF SQL EXAMPLES
-- ============================================================================
