package com.soukscan.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filtre JWT pour Spring Security 6.
 * Valide le token RS256 provenant du header Authorization: Bearer <token>
 * Injecte un UsernamePasswordAuthenticationToken dans le SecurityContext.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthFilter(JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Aucun header Authorization → continuer
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token JWT
        String token = authHeader.substring(BEARER_PREFIX.length());

        // Valider le token
        if (!jwtUtils.validateToken(token)) {
            logger.warn("Token JWT invalide ou expiré");
            sendUnauthorizedResponse(response, "Token JWT invalide ou expiré");
            return;
        }

        // Extraire les claims
        Claims claims = jwtUtils.getClaims(token);
        if (claims == null) {
            logger.warn("Impossible d'extraire les claims du token");
            sendUnauthorizedResponse(response, "Impossible d'extraire les claims du token");
            return;
        }

        // Extraire les informations utilisateur
        String userId = claims.getSubject();
        List<String> roles = jwtUtils.extractRoles(token);

        // Construire les autorités Spring Security
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> {
                    String roleName = role.startsWith("ROLE_")
                            ? role
                            : "ROLE_" + role.toUpperCase();
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList());

        // Créer l'Authentication
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        ((UsernamePasswordAuthenticationToken) authentication).setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //  LOG SÉCURISÉ (pas de CRLF injection)
        logger.debug(
                "JWT validé (userId={}, rolesCount={})",
                sanitizeForLog(userId),
                roles != null ? roles.size() : 0
        );

        filterChain.doFilter(request, response);
    }

    /**
     * Nettoyage des données utilisateur avant logging
     * (prévention CRLF / log forging)
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        return input.replaceAll("[\n\r\t]", "_");
    }

    /**
     * Envoie une réponse JSON 401 Unauthorized
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}
