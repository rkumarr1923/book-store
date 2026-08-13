package com.bookstore.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/auth/register}.
 * Validation rules match the approved OpenAPI contract.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255)
    private String email;

    /** Optional — Indian mobile format: {@code +91XXXXXXXXXX} */
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$",
             message = "Phone number must be a valid Indian mobile number (e.g. +919876543210)")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
        message = "Password must contain at least one uppercase letter, one digit, and one special character"
    )
    private String password;
}
