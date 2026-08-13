package com.bookstore.dto.response;

import com.bookstore.domain.enums.PaymentMethod;
import com.bookstore.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full payment result returned after processing a payment.
 * Used by {@code POST /api/v1/payments/process}.
 * Maps to the {@code PaymentResult} schema in the OpenAPI contract.
 * Extends PaymentRecord fields and adds the confirmed order detail.
 */
@Getter
@Builder
public class PaymentResultResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String orderNumber;
    private final PaymentMethod paymentMethod;
    private final BigDecimal payableAmount;
    private final PaymentStatus status;
    private final Instant paidAt;

    /** Full order detail with updated CONFIRMED status after successful payment. */
    private final OrderDetailResponse order;
}
