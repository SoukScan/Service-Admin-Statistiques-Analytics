package com.soukscan.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Utilitaire pour la validation et extraction de données JWT RS256.
 * Utilise la clé publique pour valider les tokens signés par l'Auth-Service.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final PublicKey publicKey;

    @Value("${security.jwt.public-key:classpath:public_key.pem}")
    private String publicKeyPath;

    public JwtUtils() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKey = loadPublicKey();
    }

    /**
     * Charge la clé publique RSA depuis le chemin configuré (classpath:public_key.pem)
     */
   private PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    try {
        ClassPathResource resource = new ClassPathResource("public_key.pem");

        // 🔥 Lecture compatible JAR : on utilise getInputStream()
        String key = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);

    } catch (Exception e) {
        logger.error("Erreur lors du chargement de la clé publique: {}", e.getMessage());
        throw e;
    }
}


    /**
     * Valide la signature RS256 du token JWT
     *
     * @param token le token JWT
     * @return true si valide, false sinon
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token JWT invalide: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrait les claims du token
     *
     * @param token le token JWT
     * @return les claims
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Erreur lors de l'extraction des claims: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrait l'userId (subject du JWT)
     *
     * @param token le token JWT
     * @return l'userId
     */
    public String extractUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extrait le username du token
     *
     * @param token le token JWT
     * @return le username
     */
    public String extractUsername(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * Extrait les rôles du token (claim "roles")
     * Retourne une liste vide si absent
     *
     * @param token le token JWT
     * @return liste des rôles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            return Collections.emptyList();
        }
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    /**
     * Extrait l'email du token
     *
     * @param token le token JWT
     * @return l'email
     */
    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("email", String.class) : null;
    }
}
