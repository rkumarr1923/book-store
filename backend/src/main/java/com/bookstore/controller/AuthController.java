package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.ForgotPasswordRequest;
import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.request.ResetPasswordRequest;
import com.bookstore.dto.response.AuthResponse;
import com.bookstore.dto.response.ForgotPasswordResponse;
import com.bookstore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>All endpoints under {@code /api/v1/auth} are public and do not require a JWT.
 * The Swagger {@code @SecurityRequirements} annotation with an empty list removes
 * the global bearer requirement from these endpoints in the generated OpenAPI spec.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/auth/register}        — register a new account</li>
 *   <li>{@code POST /api/v1/auth/login}            — authenticate and obtain a JWT</li>
 *   <li>{@code POST /api/v1/auth/forgot-password}  — request a password-reset token</li>
 *   <li>{@code POST /api/v1/auth/reset-password}   — reset password using a token (placeholder)</li>
 *   <li>{@code POST /api/v1/auth/logout}           — client-side stateless logout</li>
 * </ul>
 */
@Tag(name = "Authentication", description = "Registration, login, and password management")
@RestController
@RequestMapping(AppConstants.Api.AUTH_PATH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Register ──────────────────────────────────────────────────────────────

    @Operation(
            summary = "Register a new customer account",
            description = "Creates a new customer account and returns a JWT token and user profile."
    )
    @SecurityRequirements   // No JWT required for this endpoint
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authResponse, "Registration successful"));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Login with email or phone number",
            description = "Authenticates the user and returns a signed JWT token."
    )
    @SecurityRequirements   // No JWT required for this endpoint
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(authResponse, "Login successful"));
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Operation(
            summary = "Request a password-reset token",
            description = "Sends a password-reset token for the provided email address. "
                    + "Phase 1: the token is returned in the response. "
                    + "Phase 2: the token will be delivered via email."
    )
    @SecurityRequirements   // No JWT required for this endpoint
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        ForgotPasswordResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(response,
                "If that email is registered, a reset token has been issued."));
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Operation(
            summary = "Reset password using a token",
            description = "Resets the user's password using the token from the forgot-password flow. "
                    + "Phase 1: placeholder — validates fields but does not persist the new password."
    )
    @SecurityRequirements   // No JWT required for this endpoint
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Password has been reset successfully. Please log in with your new password."));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Logout",
            description = "Stateless logout. The client must discard the JWT. "
                    + "No server-side invalidation is performed."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }
}
