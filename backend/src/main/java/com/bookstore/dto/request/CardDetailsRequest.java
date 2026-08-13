package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Card details nested inside {@link PaymentRequest} for CREDIT_CARD / DEBIT_CARD payments.
 * Card data is validated in format only and is <strong>never persisted</strong>.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailsRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^\\d{13,19}$", message = "Card number must be 13–19 digits")
    private String cardNumber;

    @NotBlank(message = "Name on card is required")
    private String nameOnCard;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    /** Format: {@code MM/YYYY} */
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{4}$",
             message = "Expiry date must be in MM/YYYY format")
    private String expiryDate;
}
