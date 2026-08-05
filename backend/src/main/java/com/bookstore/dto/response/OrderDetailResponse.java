package com.bookstore.dto.response;

import com.bookstore.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full order detail returned by {@code GET /api/v1/orders/{orderId}}.
 * Extends the order summary with full price breakdown, delivery address snapshot,
 * coupon code, and payment status.
 * Maps to the {@code OrderDetail} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class OrderDetailResponse {

    private final Long id;
    private final String orderNumber;
    private final OrderStatus status;

    // ── Price breakdown ───────────────────────────────────────────────────────
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private final BigDecimal deliveryCharge;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;

    /** Coupon code applied to this order — {@code null} if none. */
    private final String couponCode;

    // ── Timing ───────────────────────────────────────────────────────────────
    private final Instant placedAt;
    private final LocalDate estimatedDeliveryDate;

    // ── Address & payment ─────────────────────────────────────────────────────
    private final DeliveryAddressSnapshotResponse deliveryAddress;
    private final PaymentRecordResponse payment;

    // ── Line items ────────────────────────────────────────────────────────────
    private final List<OrderItemResponse> items;
}
