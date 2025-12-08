package com.soukscan.admin.config;

import com.soukscan.admin.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security 6 pour le microservice Admin-Moderation.
 * Ce service fonctionne derrière Spring Cloud Gateway et valide uniquement les JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Chaîne de filtres de sécurité HTTP
     * - CSRF désactivé (API REST sans sessions)
     * - Sessions stateless (STATELESS)
     * - Endpoints publics: swagger, actuator
     * - Endpoints protégés: /admin/** → ROLE_ADMIN ou ROLE_MODERATOR
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (pas nécessaire pour les APIs REST stateless)
                .csrf(csrf -> csrf.disable())

                // Politique de gestion des sessions: STATELESS (pas de session côté serveur)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuration des autorisations HTTP
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/actuator/**",
                                "/health",
                                "/health/**"
                        ).permitAll()

                        // Endpoints administrateur: ROLE_ADMIN ou ROLE_MODERATOR
                        .requestMatchers("/admin/products/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers("/admin/vendors/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers("/admin/moderation/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers("/admin/actions/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Tous les autres endpoints requièrent une authentification
                        .anyRequest().authenticated()
                )

                // Ajouter le filtre JWT avant le filtre standard d'authentification
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Bean PasswordEncoder (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
