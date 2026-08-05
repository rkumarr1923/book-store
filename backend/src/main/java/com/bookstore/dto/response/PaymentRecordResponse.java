package com.bookstore.dto.response;

import com.bookstore.domain.enums.PaymentMethod;
import com.bookstore.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment record returned by {@code GET /api/v1/payments/{orderId}}.
 * Also nested inside {@link PaymentResultResponse} after a successful payment.
 * Maps to the {@code PaymentRecord} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class PaymentRecordResponse {

    private final Long paymentId;
    private final Long orderId;
    private final PaymentMethod paymentMethod;
    private final BigDecimal payableAmount;
    private final PaymentStatus status;

    /** {@code null} when payment is still pending. */
    private final Instant paidAt;

    private final Instant createdAt;
}
