package com.bookstore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Delivery address specification within {@link PlaceOrderRequest}.
 *
 * <p>The client must provide <em>either</em>:
 * <ul>
 *   <li>{@code savedAddressId} — reference to an existing saved address, or</li>
 *   <li>All inline fields: {@code firstName} through {@code email}.</li>
 * </ul>
 * The service layer validates this mutual-exclusion / completeness rule.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressRequest {

    /** Reference to a previously saved address (takes precedence over inline fields). */
    private Long savedAddressId;

    // ── Inline address fields (used when savedAddressId is null) ──────────────

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Pattern(regexp = "^\\d{6}$", message = "PIN code must be exactly 6 digits")
    private String pinCode;

    @Pattern(regexp = "^\\+91[6-9]\\d{9}$",
             message = "Phone number must be a valid Indian mobile number")
    private String phoneNumber;

    @Email(message = "Email must be a valid email address")
    @Size(max = 255)
    private String email;
}
