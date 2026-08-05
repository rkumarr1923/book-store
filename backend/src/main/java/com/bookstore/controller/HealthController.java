package com.bookstore.controller;

import com.bookstore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check endpoint.
 * GET /api/v1/health — returns application status and version.
 */
@Tag(name = "Health", description = "Application health check")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Value("${spring.application.name:book-store-backend}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @SecurityRequirements
    @Operation(
            summary = "Application health check",
            description = "Returns application status, name, and server timestamp. "
                    + "Used to verify the application is running correctly."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> payload = Map.of(
                "status",      "UP",
                "application", applicationName,
                "version",     "1.0.0-SNAPSHOT",
                "timestamp",   Instant.now().toString(),
                "port",        serverPort
        );
        return ResponseEntity.ok(ApiResponse.ok(payload, "Book Store API is running"));
    }
}
