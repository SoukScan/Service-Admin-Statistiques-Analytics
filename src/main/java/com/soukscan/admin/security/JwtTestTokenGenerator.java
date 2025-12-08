package com.soukscan.admin.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * ⚠️ UTILITAIRE DE TEST UNIQUEMENT
 * 
 * Générateur de tokens JWT pour tester l'API en développement.
 * NE PAS utiliser en production !
 * 
 * En production, les tokens sont générés par l'Auth-Service avec la clé privée RSA.
 */
public class JwtTestTokenGenerator {

    // ⚠️ À remplacer par la clé utilisée par l'Auth-Service en production
    private static final String SECRET_KEY = "your-256-bit-secret-key-for-testing-purposes-12345";

    /**
     * Génère un token valide pour tester
     * 
     * Exemple:
     * String token = JwtTestTokenGenerator.generateToken(
     *     "user-123",
     *     "john_doe",
     *     List.of("ADMIN", "MODERATOR"),
     *     3600  // 1 heure
     * );
     * 
     * curl -H "Authorization: Bearer {token}" http://localhost:8090/admin/products
     */
    public static String generateToken(String userId, String username, List<String> roles, int expirationSeconds) {
        return generateTokenWithEmail(userId, username, username + "@example.com", roles, expirationSeconds);
    }

    /**
     * Génère un token avec email personnalisé
     */
    public static String generateTokenWithEmail(String userId, String username, String email, 
                                                List<String> roles, int expirationSeconds) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("email", email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(
                    Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)),
                    SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Génère un token expiré (pour tester le rejet)
     */
    public static String generateExpiredToken(String userId, String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))  // 2 heures ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000))  // 1 heure ago
                .signWith(
                    Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)),
                    SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Génère un token sans rôles (pour tester la gestion vide)
     */
    public static String generateTokenWithoutRoles(String userId, String username, int expirationSeconds) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("email", username + "@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(
                    Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)),
                    SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Génère un token ADMIN valide
     * Utilisation: curl -H "Authorization: Bearer {token}" ...
     */
    public static String generateAdminToken() {
        return generateToken(
            "admin-001",
            "admin_user",
            List.of("ADMIN", "MODERATOR"),
            3600  // 1 heure
        );
    }

    /**
     * Génère un token MODERATOR valide
     */
    public static String generateModeratorToken() {
        return generateToken(
            "moderator-001",
            "moderator_user",
            List.of("MODERATOR"),
            3600  // 1 heure
        );
    }

    /**
     * Génère un token USER valide
     */
    public static String generateUserToken() {
        return generateToken(
            "user-001",
            "regular_user",
            List.of("USER"),
            3600  // 1 heure
        );
    }

    /**
     * Main pour générer des tokens rapidement
     * 
     * Usage:
     * java -cp target/classes com.soukscan.admin.security.JwtTestTokenGenerator
     */
    public static void main(String[] args) {
        System.out.println("=== JWT Test Token Generator ===\n");

        // ADMIN token
        String adminToken = generateAdminToken();
        System.out.println("ADMIN Token (1 heure):");
        System.out.println("Authorization: Bearer " + adminToken);
        System.out.println();

        // MODERATOR token
        String moderatorToken = generateModeratorToken();
        System.out.println("MODERATOR Token (1 heure):");
        System.out.println("Authorization: Bearer " + moderatorToken);
        System.out.println();

        // USER token
        String userToken = generateUserToken();
        System.out.println("USER Token (1 heure):");
        System.out.println("Authorization: Bearer " + userToken);
        System.out.println();

        // Expired token
        String expiredToken = generateExpiredToken("expired-user", "expired", List.of("ADMIN"));
        System.out.println("EXPIRED Token (pour test rejet):");
        System.out.println("Authorization: Bearer " + expiredToken);
        System.out.println();

        // Custom token
        String customToken = generateToken(
            "custom-123",
            "custom_user",
            List.of("CUSTOM_ROLE"),
            7200  // 2 heures
        );
        System.out.println("CUSTOM Token (2 heures):");
        System.out.println("Authorization: Bearer " + customToken);
        System.out.println();

        System.out.println("=== Usage Examples ===\n");

        System.out.println("Test ADMIN endpoint:");
        System.out.println("curl -H \"Authorization: Bearer " + adminToken + "\" \\");
        System.out.println("  http://localhost:8090/admin/products\n");

        System.out.println("Test Swagger (sans token):");
        System.out.println("curl http://localhost:8090/swagger-ui.html\n");

        System.out.println("Test expired token (doit retourner 401):");
        System.out.println("curl -H \"Authorization: Bearer " + expiredToken + "\" \\");
        System.out.println("  http://localhost:8090/admin/products\n");

        System.out.println("✅ Copier les tokens dans vos requêtes curl");
        System.out.println("⚠️  Les tokens expirent dans le temps indiqué");
    }
}
