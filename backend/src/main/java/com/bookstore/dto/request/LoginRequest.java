package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 * {@code identifier} accepts either an email address or a phone number.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Identifier (email or phone number) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
