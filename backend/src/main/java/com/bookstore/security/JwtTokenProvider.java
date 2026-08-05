package com.bookstore.security;

import com.bookstore.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component responsible for JWT token generation, validation, and claims extraction.
 *
 * <p>Uses HMAC-SHA256 (HS256) signing with the secret configured in {@code app.jwt.secret}.
 * The subject of every token is the user's email address.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    // ── Token Generation ──────────────────────────────────────────────────────

    /**
     * Generate a signed JWT for the authenticated principal.
     *
     * @param authentication the authenticated {@link Authentication} object
     * @return signed JWT string
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(principal.getUsername());
    }

    /**
     * Generate a signed JWT directly from an email address.
     * Used after registration when no {@link Authentication} context exists yet.
     *
     * @param email the user's email address
     * @return signed JWT string
     */
    public String generateTokenFromEmail(String email) {
        return buildToken(email);
    }

    // ── Claims Extraction ─────────────────────────────────────────────────────

    /**
     * Extract the subject (email) from a JWT token.
     *
     * @param token the JWT string
     * @return the email encoded as the token subject
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validate that the token is well-formed, signed with the correct key, and not expired.
     *
     * @param token the JWT string to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT token is unsupported: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT token is malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature is invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private String buildToken(String subject) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getExpirationMs());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
