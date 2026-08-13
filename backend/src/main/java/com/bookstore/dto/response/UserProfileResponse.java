package com.bookstore.dto.response;

import com.bookstore.domain.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO returned for the authenticated user's profile.
 * Used by {@code GET /api/v1/users/me} and {@code PUT /api/v1/users/me}.
 * Also nested inside {@link AuthResponse}.
 */
@Getter
@Builder
public class UserProfileResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final UserRole role;
    private final Instant createdAt;
}
