package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for a saved delivery address.
 * Used by all address endpoints ({@code GET}, {@code POST}, {@code PUT}, {@code PATCH}).
 */
@Getter
@Builder
public class AddressResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String country;
    private final String pinCode;
    private final String phoneNumber;
    private final String email;
    private final boolean isDefault;
    private final Instant createdAt;
}
