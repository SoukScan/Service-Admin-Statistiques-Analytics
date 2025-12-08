package com.soukscan.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 (Swagger) Configuration for Admin-Moderation Service
 *
 * Configures:
 * - JWT Bearer token security scheme
 * - API information and contact
 * - Server endpoints
 * - Global security requirements
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure OpenAPI 3 bean with security scheme, API info, and servers
     */
    @Bean
    public OpenAPI adminAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Build API Info with title, description, version, contact, and license
     */
    private Info buildInfo() {
        return new Info()
                .title("SoukScan – Admin, Moderation & Analytics API")
                .description("""
                        Microservice d'administration, modération et statistiques de la plateforme SoukScan.
                        
                        ## Features
                        - **Vendor Management** - Verify, reject, suspend, and activate vendors
                        - **Product Management** - Create, update, delete, and search products
                        - **Moderation** - Handle user reports, approve/reject actions, warn users, block accounts
                        - **Admin Logs** - Track all administrative actions
                        - **Statistics** - Global and user-specific analytics
                        
                        ## Authentication
                        All endpoints (except public ones) require JWT Bearer token in the Authorization header:
                        ```
                        Authorization: Bearer <JWT_TOKEN>
                        ```
                        
                        ## Required Roles
                        - `ROLE_ADMIN` - Full administrative access
                        - `ROLE_MODERATOR` - Moderation actions only
                        
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("SoukScan Admin Team")
                        .email("admin@soukscan.com")
                        .url("https://soukscan.com/support"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * Build server configurations
     */
    private java.util.List<Server> buildServers() {
        return java.util.List.of(
                new Server()
                        .url("http://localhost:8090")
                        .description("Local Development"),
                new Server()
                        .url("https://api-dev.soukscan.com")
                        .description("Development Environment"),
                new Server()
                        .url("https://api-staging.soukscan.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.soukscan.com")
                        .description("Production Environment")
        );
    }

    /**
     * Build Components with SecurityScheme for JWT Bearer
     */
    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("""
                                JWT Bearer token for API authentication.
                                
                                **How to obtain:**
                                1. Call the Auth-Service login endpoint
                                2. Include the returned token in the Authorization header
                                
                                **Format:**
                                ```
                                Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
                                ```
                                
                                **Token contains:**
                                - `sub`: User ID
                                - `username`: Username
                                - `roles`: Array of roles (ADMIN, MODERATOR)
                                - `email`: User email
                                - `iat`: Issued At timestamp
                                - `exp`: Expiration timestamp
                                """)
                );
    }
}
