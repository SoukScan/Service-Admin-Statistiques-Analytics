# 🚀 Démarrage Rapide - Docker

## 30 secondes pour démarrer le stack complet

### Prérequis
- ✅ Docker Desktop installé (https://www.docker.com/products/docker-desktop)
- ✅ Docker Compose >= 2.0 (inclus avec Docker Desktop)
- ✅ Port 8090 disponible
- ✅ `public_key.pem` présent à la racine du projet

### Vérification rapide
```bash
docker --version
docker-compose --version
```

---

## 🔥 Lancer le tout en une commande

```bash
cd admin-moderation-service
docker-compose up --build
```

**C'est tout !** Attendez les logs:
```
admin-moderation-service | Started AdminServiceApplication in X seconds
```

---

## 🌐 Tester immédiatement

```bash
# Health check (retourne {"status":"UP"})
curl http://localhost:8090/actuator/health

# Swagger UI
open http://localhost:8090/swagger-ui.html
```

---

## 📦 Qu'est-ce qui démarre?

| Service | Port | Status |
|---------|------|--------|
| PostgreSQL | 5432 | `healthy` |
| Zookeeper | 2181 | `healthy` |
| Kafka | 9092 | `healthy` |
| Admin Service | 8090 | `UP` |

---

## 🛑 Arrêter proprement

```bash
# Ctrl+C dans le terminal (ou dans une autre fenêtre):
docker-compose down
```

---

## 📊 Voir les logs en temps réel

```bash
# Tout
docker-compose logs -f

# Juste le service
docker-compose logs -f admin-moderation-service

# Juste la DB
docker-compose logs -f postgres
```

---

## 🔧 Commandes utiles

```bash
# Redémarrer le service
docker-compose restart admin-moderation-service

# Voir l'état des containers
docker-compose ps

# Ressources consommées
docker stats

# Shell PostgreSQL
docker-compose exec postgres psql -U admin -d admin_db
```

---

## 💡 Utilisez les scripts helper

### Sur Windows
```bash
.\docker-commands.bat help
.\docker-commands.bat up
.\docker-commands.bat logs-app
.\docker-commands.bat health
```

### Sur Linux/Mac
```bash
chmod +x docker-commands.sh
./docker-commands.sh help
./docker-commands.sh up
./docker-commands.sh logs-app
./docker-commands.sh health
```

---

## ✅ Checklist premier lancement

- [ ] `docker-compose up --build` lancé
- [ ] Logs affichent "Started AdminServiceApplication"
- [ ] `curl http://localhost:8090/actuator/health` retourne UP
- [ ] PostgreSQL est healthy
- [ ] Kafka est running
- [ ] Pas d'erreurs de connexion

---

## 🆘 Ça ne démarre pas?

### Erreur: "Port already in use"
```bash
# Libérer le port 8090
# Windows PowerShell:
netstat -ano | findstr :8090
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8090
kill -9 <PID>

# Ou utiliser un autre port dans docker-compose.yaml
# ports:
#   - "8091:8090"
```

### Erreur: "Connection refused"
```bash
# Vérifier que les services sont UP
docker-compose ps

# Peut prendre 30-40 secondes au premier démarrage
# Attendez avant de tester les endpoints
```

### Erreur: "Liquibase lock"
```bash
docker-compose down -v
docker-compose up --build
```

---

## 📖 Documentation complète

Lire `DOCKER_DEPLOYMENT_GUIDE.md` pour:
- Configuration détaillée
- Troubleshooting avancé
- Monitoring
- Production deployment
- Sécurité

---

**Status:** ✅ Ready to deploy  
**Last update:** 2025-12-08
