package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response body for {@code POST /api/v1/auth/register} and {@code POST /api/v1/auth/login}.
 * The JWT token plus a minimal user profile are returned together so the client
 * can bootstrap its state without a second network call.
 */
@Getter
@Builder
public class AuthResponse {

    /** Signed JWT Bearer token. */
    private final String token;

    /** Basic user information needed for the UI. */
    private final UserProfileResponse user;
}
