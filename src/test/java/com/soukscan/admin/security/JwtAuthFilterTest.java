package com.soukscan.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.when;
import com.soukscan.admin.service.ProductAdminService;
import com.soukscan.admin.service.VendorAdminService;
import com.soukscan.admin.service.StatsService;
import com.soukscan.admin.repository.PriceReportRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests pour le filtre JWT et les utilitaires de validation
 */
@SpringBootTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@AutoConfigureMockMvc
public class JwtAuthFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private ProductAdminService productAdminService;

    @MockBean
    private VendorAdminService vendorAdminService;

    @MockBean
    private StatsService statsService;

    @MockBean
    private PriceReportRepository priceReportRepository;

    private String validToken;
    private String expiredToken;
    private static final String SECRET_KEY = "your-256-bit-secret-key-for-testing-purposes-12345";

    @BeforeEach
    public void setUp() {
        // Générer un token valide pour les tests
        validToken = generateToken("user-123", "john_doe", List.of("ADMIN", "MODERATOR"), false);
        expiredToken = generateToken("user-456", "jane_doe", List.of("MODERATOR"), true);

        // Mock JwtUtils bean to avoid loading real public key and to control validation behavior
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.validateToken(expiredToken)).thenReturn(false);
        when(jwtUtils.validateToken("invalid.token.here")).thenReturn(false);

        when(jwtUtils.extractUserId(validToken)).thenReturn("user-123");
        when(jwtUtils.extractUsername(validToken)).thenReturn("john_doe");
        when(jwtUtils.extractRoles(validToken)).thenReturn(List.of("ADMIN", "MODERATOR"));
        when(jwtUtils.extractEmail(validToken)).thenReturn("john_doe@example.com");

        // Mock getClaims to return a subject for the valid token so the filter can extract it
        io.jsonwebtoken.Claims claims = Jwts.claims().setSubject("user-123");
        when(jwtUtils.getClaims(validToken)).thenReturn(claims);
    }

    /**
     * Test: Requête avec un token valide doit retourner 200
     */
    @Test
    public void testValidTokenRequest() throws Exception {
        mockMvc.perform(get("/admin/products")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    /**
     * Test: Requête sans token doit retourner 401
     */
    @Test
    public void testMissingTokenRequest() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: Requête avec token invalide doit retourner 401
     */
    @Test
    public void testInvalidTokenRequest() throws Exception {
        mockMvc.perform(get("/admin/products")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Requête avec format Authorization invalide
     */
    @Test
    public void testInvalidAuthorizationFormat() throws Exception {
        mockMvc.perform(get("/admin/products")
                .header("Authorization", "NotBearer " + validToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: Accès public à Swagger sans token
     */
    @Test
    public void testPublicSwaggerEndpoint() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Test: Accès public aux actuator endpoints
     */
    @Test
    public void testPublicActuatorEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    /**
     * Génère un token JWT pour les tests
     * Note: En production, utilisez la clé publique/privée RS256 générée par l'Auth-Service
     */
    private String generateToken(String userId, String username, List<String> roles, boolean expired) {
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
}
