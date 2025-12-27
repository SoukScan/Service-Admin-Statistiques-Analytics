package com.soukscan.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Utilitaire JWT (HS256)
 * - Le token est signé par Auth-Service avec clé symétrique
 * - Ce service ne fait QUE la validation avec la clé secrète partagée
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String secret;

    public JwtUtils(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret;
        logger.info("Clé secrète JWT chargée avec succès");
    }

    /**
     * Valide la signature HS256 du JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré");
            return false;

        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token JWT invalide");
            return false;
        }
    }

    /**
     * Extraction des claims
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Impossible d'extraire les claims JWT");
            return null;
        }
    }

    public String extractUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractUsername(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        if (claims == null) return Collections.emptyList();

        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("email", String.class) : null;
    }
}
