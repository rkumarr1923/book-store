package com.bookstore.config;

import com.bookstore.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <ul>
 *   <li>Stateless session management (no {@link jakarta.servlet.http.HttpSession}).</li>
 *   <li>JWT filter added before {@link UsernamePasswordAuthenticationFilter}.</li>
 *   <li>Public endpoints: Swagger UI, Actuator health, auth, and read-only catalogue.</li>
 *   <li>All other endpoints require a valid Bearer JWT.</li>
 *   <li>Authentication entry point returns {@code 401 Unauthorized} (JSON-friendly).</li>
 *   <li>Access-denied handler returns {@code 403 Forbidden}.</li>
 *   <li>CORS is handled by {@link WebMvcConfig}; the Security layer just enables it.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ── Public Paths ──────────────────────────────────────────────────────────

    /** Endpoints accessible without a JWT token. */
    private static final String[] PUBLIC_PATHS = {
            // Swagger UI
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            // Actuator health
            "/actuator/health",
            "/actuator/info",
            // Health check
            "/api/v1/health",
            // Authentication
            "/api/v1/auth/**"
    };

    /** Public read-only catalogue endpoints (GET only). */
    private static final String[] PUBLIC_GET_PATHS = {
            "/api/v1/home/**",
            "/api/v1/genres/**",
            "/api/v1/books/**",
            "/api/v1/authors/**"
    };

    // ── Filter Chain ──────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — stateless JWT API
                .csrf(AbstractHttpConfigurer::disable)

                // Delegate CORS to WebMvcConfig
                .cors(cors -> cors.configure(http))

                // Stateless session — no HttpSession created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        .anyRequest().authenticated()
                )

                // Return 401 as JSON-compatible response (no redirect to login page)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, denied) ->
                                res.sendError(HttpStatus.FORBIDDEN.value(),
                                        "Access denied"))
                )

                // JWT filter runs before the standard username/password filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Authentication Infrastructure ─────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
