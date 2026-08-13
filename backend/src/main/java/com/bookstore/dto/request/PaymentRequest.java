package com.bookstore.dto.request;

import com.bookstore.domain.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/payments/process}.
 *
 * <p>Conditional field rules (enforced by the service layer):
 * <ul>
 *   <li>{@code CREDIT_CARD} / {@code DEBIT_CARD} → {@code cardDetails} required</li>
 *   <li>{@code UPI} → {@code upiDetails} required</li>
 *   <li>{@code WALLET} → no additional fields needed</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /** Required when {@code paymentMethod} is {@code CREDIT_CARD} or {@code DEBIT_CARD}. */
    @Valid
    private CardDetailsRequest cardDetails;

    /** Required when {@code paymentMethod} is {@code UPI}. */
    @Valid
    private UpiDetailsRequest upiDetails;
}
