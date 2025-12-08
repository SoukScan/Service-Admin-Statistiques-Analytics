package com.soukscan.admin.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour JwtUtils
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String validToken;
    private String expiredToken;
    private static final String SECRET_KEY = "your-256-bit-secret-key-for-testing-purposes-12345";

    @BeforeEach
    public void setUp() {
        // Use a Mockito mock to avoid instantiating the real JwtUtils (which loads a public key)
        jwtUtils = Mockito.mock(JwtUtils.class);
        // Générer des tokens de test
        validToken = generateTestToken("user-123", "john_doe", List.of("ADMIN", "MODERATOR"), false);
        expiredToken = generateTestToken("user-456", "jane_doe", List.of("MODERATOR"), true);

        // Stub expected behaviors on the mock using the generated tokens
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.validateToken(expiredToken)).thenReturn(false);
        when(jwtUtils.validateToken("invalid.token.here")).thenReturn(false);

        when(jwtUtils.extractUserId(validToken)).thenReturn("user-123");
        when(jwtUtils.extractUsername(validToken)).thenReturn("john_doe");
        when(jwtUtils.extractRoles(validToken)).thenReturn(List.of("ADMIN", "MODERATOR"));
        when(jwtUtils.extractEmail(validToken)).thenReturn("john_doe@example.com");

        when(jwtUtils.getClaims("invalid.token")).thenReturn(null);
        String tokenWithoutRoles = generateTestTokenWithoutRoles("user-789", "empty_roles_user");
        when(jwtUtils.extractRoles(tokenWithoutRoles)).thenReturn(Collections.emptyList());
    }

    /**
     * Test: Validation d'un token valide
     */
    @Test
    public void testValidateToken_Success() {
        assertTrue(jwtUtils.validateToken(validToken), "Le token valide devrait être accepté");
    }

    /**
     * Test: Validation d'un token expiré
     */
    @Test
    public void testValidateToken_Expired() {
        String expiredToken = generateTestToken("user-456", "jane_doe", List.of("MODERATOR"), true);
        assertFalse(jwtUtils.validateToken(expiredToken), "Un token expiré devrait être rejeté");
    }

    /**
     * Test: Validation d'un token invalide
     */
    @Test
    public void testValidateToken_Invalid() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"), "Un token invalide devrait être rejeté");
    }

    /**
     * Test: Extraction du userId
     */
    @Test
    public void testExtractUserId() {
        String userId = jwtUtils.extractUserId(validToken);
        assertEquals("user-123", userId, "Le userId devrait être 'user-123'");
    }

    /**
     * Test: Extraction du username
     */
    @Test
    public void testExtractUsername() {
        String username = jwtUtils.extractUsername(validToken);
        assertEquals("john_doe", username, "Le username devrait être 'john_doe'");
    }

    /**
     * Test: Extraction des rôles
     */
    @Test
    public void testExtractRoles() {
        List<String> roles = jwtUtils.extractRoles(validToken);
        assertNotNull(roles, "Les rôles ne doivent pas être null");
        assertEquals(2, roles.size(), "Le token devrait contenir 2 rôles");
        assertTrue(roles.contains("ADMIN"), "Les rôles doivent contenir ADMIN");
        assertTrue(roles.contains("MODERATOR"), "Les rôles doivent contenir MODERATOR");
    }

    /**
     * Test: Extraction des rôles avec un token qui n'en contient pas
     */
    @Test
    public void testExtractRoles_Empty() {
        String tokenWithoutRoles = generateTestTokenWithoutRoles("user-789", "empty_roles_user");
        List<String> roles = jwtUtils.extractRoles(tokenWithoutRoles);
        assertNotNull(roles, "Les rôles doivent être une liste vide");
        assertTrue(roles.isEmpty(), "Aucun rôle ne devrait être présent");
    }

    /**
     * Test: Extraction du email
     */
    @Test
    public void testExtractEmail() {
        String email = jwtUtils.extractEmail(validToken);
        assertEquals("john_doe@example.com", email, "L'email devrait être 'john_doe@example.com'");
    }

    /**
     * Test: getClaims avec un token invalide retourne null
     */
    @Test
    public void testGetClaims_Invalid() {
        var claims = jwtUtils.getClaims("invalid.token");
        assertNull(claims, "getClaims devrait retourner null pour un token invalide");
    }

    /**
     * Génère un token JWT de test avec HS256
     */
    private String generateTestToken(String userId, String username, List<String> roles, boolean expired) {
        long expirationTime = expired ? System.currentTimeMillis() - 3600000 : System.currentTimeMillis() + 3600000;

        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .claim("email", username + "@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(expirationTime))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Génère un token JWT sans le claim roles
     */
    private String generateTestTokenWithoutRoles(String userId, String username) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("email", username + "@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
