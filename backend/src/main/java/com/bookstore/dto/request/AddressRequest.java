package com.bookstore.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/users/me/addresses}
 * and {@code PUT /api/v1/users/me/addresses/{addressId}}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^\\d{6}$", message = "PIN code must be exactly 6 digits")
    private String pinCode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$",
             message = "Phone number must be a valid Indian mobile number (e.g. +919876543210)")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255)
    private String email;

    /** When {@code true}, this address becomes the default delivery address. */
    private boolean isDefault;
}
