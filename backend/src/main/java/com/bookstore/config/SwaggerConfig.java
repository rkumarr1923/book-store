package com.bookstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger UI configuration.
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api/v1")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.bookstore.com/api/v1")
                                .description("Production server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Book Store API")
                .description("""
                        Complete REST API for the Book Store eCommerce application.
                        
                        **Authentication:** Protected endpoints require a JWT Bearer token.
                        Obtain a token via `POST /api/v1/auth/login` or `POST /api/v1/auth/register`.
                        
                        **Phase 1 Notes:**
                        - Payment processing uses a mock implementation.
                        - All prices are in Indian Rupees (INR / ₹).
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Book Store API Team")
                        .email("api@bookstore.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://bookstore.com"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(BEARER_AUTH_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT access token obtained from /auth/login or /auth/register. "
                        + "Prefix value with 'Bearer '.");
    }
}
