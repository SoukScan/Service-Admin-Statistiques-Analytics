# ⚡ Guide Rapide - JWT Implementation

## 🚀 5 Minutes pour Comprendre

### 1. Architecture Simple
```
Token JWT (signé RS256 par Auth-Service)
    ↓
JwtAuthFilter (OncePerRequestFilter)
    ↓
JwtUtils.validateToken() → true/false
    ↓
Injection dans SecurityContext
    ↓
SecurityConfig: Autorisation basée rôles
```

---

## 📝 Code Principal

### JwtAuthFilter (Entrée)
```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;  // Injection
    
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) {
        // 1. Lire Authorization: Bearer <token>
        String token = extractToken(req.getHeader("Authorization"));
        
        // 2. Valider avec JwtUtils
        if (!jwtUtils.validateToken(token)) {
            sendError401(res);
            return;
        }
        
        // 3. Extraire userId et rôles
        String userId = jwtUtils.extractUserId(token);
        List<String> roles = jwtUtils.extractRoles(token);
        
        // 4. Créer Authentication
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(userId, null, roles);
        
        // 5. Injecter dans SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 6. Continuer la chaîne
        fc.doFilter(req, res);
    }
}
```

### JwtUtils (Utilitaires)
```java
@Component
public class JwtUtils {
    private PublicKey publicKey;  // Chargée depuis public_key.pem
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)  // Clé RSA
                .build()
                .parseClaimsJws(token);    // Vérifier signature
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String extractUserId(String token) {
        return getClaims(token).getSubject();  // claim: sub
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);  // claim: roles
    }
}
```

### SecurityConfig (Autorisations)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/actuator/**").permitAll()  // Public
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERATOR")  // Protégé
                .anyRequest().authenticated()  // Par défaut authentifié
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 🧪 Exemples Curls

### ✅ Avec Token Valide
```bash
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:8090/admin/products
# Réponse: 200 OK + données
```

### ❌ Sans Token
```bash
curl http://localhost:8090/admin/products
# Réponse: 401 {error: "Unauthorized"}
```

### ❌ Token Expiré
```bash
curl -H "Authorization: Bearer expired_token" http://localhost:8090/admin/products
# Réponse: 401 {error: "Token JWT invalide ou expiré"}
```

### ✅ Endpoint Public
```bash
curl http://localhost:8090/swagger-ui.html
# Réponse: 200 OK (pas besoin de token)
```

---

## 📦 JWT Claims Expected

Le token signé par l'Auth-Service doit contenir:

```json
{
  "sub": "user-123",                    // userId (subject)
  "username": "john_doe",               // username
  "email": "john@example.com",          // email
  "roles": ["ADMIN", "MODERATOR"],      // roles (tableau)
  "iat": 1234567890,                    // issued at
  "exp": 1234571490                     // expiration
}
```

---

## 🔧 Fichiers à Configurer

### 1. Clé Publique (src/main/resources/public_key.pem)
```
Copier depuis Auth-Service et remplacer le fichier
```

### 2. Configuration (src/main/resources/application.yml)
```yaml
security:
  jwt:
    public-key: classpath:public_key.pem
```

### 3. Logging (Optionnel, pour debug)
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.soukscan.admin: DEBUG
```

---

## ✅ Test Rapide

```bash
# 1. Compiler
mvn clean compile

# 2. Tester
mvn test

# 3. Run
mvn spring-boot:run

# 4. Accéder à l'API
curl http://localhost:8090/swagger-ui.html
```

---

## 🎯 Rôles Supportés

| Rôle | Endpoint | Accès |
|------|----------|-------|
| ADMIN | /admin/** | ✅ Tous |
| MODERATOR | /admin/moderation/** | ✅ Modération |
| MODERATOR | /admin/products/** | ✅ Produits |
| PUBLIC | /swagger-ui/**, /actuator/** | ✅ Publique |

---

## 🔐 Sécurité en Production

1. **Générer clés RSA 2048-bit**
   ```bash
   openssl genrsa -out private.pem 2048
   openssl rsa -in private.pem -pubout -out public.pem
   ```

2. **Partager SEULEMENT public.pem** avec Admin-Service

3. **Auth-Service signe avec private.pem** (jamais partagé)

4. **Admin-Service valide avec public.pem**

---

## 📊 Flux Complet

```
1. User → Auth-Service
   POST /auth/login
   {username, password}
   
2. Auth-Service → User
   {token: "eyJ..."}  (signé avec private.pem + RS256)
   
3. User → Admin-Service
   GET /admin/products
   Authorization: Bearer eyJ...
   
4. JwtAuthFilter
   ├─ Extract token
   ├─ JwtUtils.validateToken() ← Vérifier signature RS256
   ├─ JwtUtils.extractRoles() ← Extraire rôles
   └─ Créer UsernamePasswordAuthenticationToken
   
5. SecurityConfig
   ├─ hasAnyRole("ADMIN", "MODERATOR") ?
   └─ Autoriser ou 403 Forbidden
   
6. Admin-Service → User
   {data: [...]}  ✅ 200 OK
```

---

## 🐛 Erreurs Courantes

| Erreur | Cause | Solution |
|--------|-------|----------|
| "Could not resolve public key" | public_key.pem manquant | Copier depuis Auth-Service |
| "Invalid JWT signature" | Clé publique ne correspond pas | Vérifier que c'est la bonne clé |
| "Token expired" | Token trop vieux | Régénérer un nouveau token |
| "Claims is null" | Token invalide | Vérifier signature RS256 |
| 401 sans token | Endpoint protégé | Ajouter Authorization header |

---

## 🎓 Résumé

| Concept | Fichier | Rôle |
|---------|---------|------|
| Filtre | JwtAuthFilter.java | Lire et valider token |
| Utilitaires | JwtUtils.java | Extraire claims, valider signature |
| Configuration | SecurityConfig.java | Autorisation basée rôles |
| Clé | public_key.pem | Valider signature RS256 |
| Config | application.yml | Chemin de la clé |

---

## ✅ Checklist Finale

- [x] JwtAuthFilter implémenté
- [x] JwtUtils implémenté
- [x] SecurityConfig configuré
- [x] public_key.pem présent
- [x] application.yml mis à jour
- [x] `mvn clean compile` → SUCCESS
- [x] Tests passent
- [x] Documentation complète

---

**C'est tout ! Vous avez une sécurité JWT complète et robuste.** 🎉
