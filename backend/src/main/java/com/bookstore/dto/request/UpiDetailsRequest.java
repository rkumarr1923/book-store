package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UPI details nested inside {@link PaymentRequest} for UPI payments.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiDetailsRequest {

    /** Valid UPI VPA format: {@code handle@bank}. */
    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$",
             message = "UPI ID must be in the format handle@bank (e.g. arjun@okaxis)")
    private String upiId;
}
