package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response body for {@code POST /api/v1/auth/forgot-password}.
 * Phase 1: the reset token is returned directly in the response.
 * Phase 2: the token will be sent via email and this DTO will carry only
 * a confirmation message.
 */
@Getter
@Builder
public class ForgotPasswordResponse {

    /** The one-time reset token (Phase 1 — returned directly). */
    private final String resetToken;

    /** Seconds until the token expires (e.g. 900 = 15 minutes). */
    private final int expiresIn;
}
