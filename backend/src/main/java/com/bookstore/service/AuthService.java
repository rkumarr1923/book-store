package com.bookstore.service;

import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.config.AppProperties;
import com.bookstore.domain.User;
import com.bookstore.domain.enums.UserRole;
import com.bookstore.dto.request.ForgotPasswordRequest;
import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.request.ResetPasswordRequest;
import com.bookstore.dto.response.AuthResponse;
import com.bookstore.dto.response.ForgotPasswordResponse;
import com.bookstore.mapper.UserMapper;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication service — handles registration, login, password-reset flow, and logout.
 *
 * <p>Stateless design: the service never stores sessions or token black-lists.
 * Logout is handled entirely on the client side by discarding the JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtTokenProvider    jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper          userMapper;
    private final AppProperties       appProperties;
    private final EmailService        emailService;

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Register a new customer account.
     *
     * <ol>
     *   <li>Validate email and phone uniqueness.</li>
     *   <li>Hash the raw password.</li>
     *   <li>Persist the new {@link User}.</li>
     *   <li>Return a signed JWT and the user profile.</li>
     * </ol>
     *
     * @param request registration form fields
     * @return {@link AuthResponse} containing the JWT and a minimal user profile
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }

        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException(
                    "An account with phone number '" + request.getPhoneNumber() + "' already exists.");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        user = userRepository.save(user);
        log.info("Registered new user with email '{}'", user.getEmail());

        // Send welcome email asynchronously (fire-and-forget; never blocks registration)
        emailService.sendWelcomeEmail(user);

        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toProfileResponse(user))
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticate a user using email or phone number as the identifier.
     *
     * <p>The {@code identifier} field may be either an email address or a phone number.
     * Lookup by phone resolves the email which is then used as the Spring Security username.
     *
     * @param request login credentials
     * @return {@link AuthResponse} containing the JWT and a minimal user profile
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();

        // Resolve the email address — the Spring Security username
        String email = resolveEmail(identifier);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getEmail()));

        log.info("User '{}' logged in", email);
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toProfileResponse(user))
                .build();
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    /**
     * Initiate the password-reset flow for the given email address.
     *
     * <p>Phase 1 implementation: a UUID reset token is generated and returned directly
     * in the response. In a production build this token would be emailed to the user
     * and the response would carry only a confirmation message.
     *
     * <p>The endpoint always returns a successful response regardless of whether the
     * email exists, to prevent user-enumeration attacks.
     *
     * @param request contains the email address
     * @return {@link ForgotPasswordResponse} with the reset token and its TTL
     */
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        // Intentional: no exception thrown when email is not found (anti-enumeration)
        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
                log.info("Password reset requested for '{}'", user.getEmail())
        );

        String resetToken = UUID.randomUUID().toString();
        int    expiresIn  = appProperties.getPasswordReset().getTokenExpirySeconds();

        // Phase 1: return the token directly. Phase 2: send via email.
        return ForgotPasswordResponse.builder()
                .resetToken(resetToken)
                .expiresIn(expiresIn)
                .build();
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    /**
     * Placeholder reset-password implementation.
     *
     * <p>Phase 1: validates the request fields (token, new password, confirm password match)
     * but does not persist a password change because no token-to-user mapping store exists yet.
     * The full implementation requires a {@code PasswordResetToken} entity or a cache layer
     * and will be completed in a later phase.
     *
     * @param request contains the reset token and new password fields
     */
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }
        // Phase 1 placeholder: token lookup and persistence deferred to a later phase.
        log.info("Reset password invoked (placeholder) — token: {}", request.getResetToken());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Stateless logout — no server-side action required.
     *
     * <p>The client is responsible for discarding the JWT. This method exists so
     * an explicit {@code POST /auth/logout} endpoint can be documented in the API
     * contract and later extended with a token block-list if needed.
     */
    public void logout() {
        // Stateless: client discards the JWT; nothing to do on the server.
        log.debug("Logout called — stateless; no server-side action taken.");
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Resolve the Spring Security username (email) from an identifier that may be
     * either an email address or an Indian phone number.
     */
    private String resolveEmail(String identifier) {
        // If the identifier looks like a phone number, resolve via phone lookup
        if (identifier.startsWith("+")) {
            return userRepository.findByPhoneNumber(identifier)
                    .map(User::getEmail)
                    .orElse(identifier); // fallback — Spring Security will reject it gracefully
        }
        return identifier;
    }
}
