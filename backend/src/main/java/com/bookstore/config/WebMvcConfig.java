package com.bookstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration.
 * Configures CORS to allow the React frontend to call the API.
 *
 * <p>Allowed origin is read from {@code CORS_ALLOWED_ORIGIN} env var (default: localhost dev).
 * Set this to the deployed frontend URL in production, e.g. {@code https://bookstore.onrender.com}.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin:http://localhost:3000}")
    private String corsAllowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:*",  // Vite dev server
                        corsAllowedOrigin      // Production frontend (set via CORS_ALLOWED_ORIGIN env var)
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
