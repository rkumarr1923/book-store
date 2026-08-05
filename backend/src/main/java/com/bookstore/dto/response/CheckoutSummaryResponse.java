package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Checkout summary returned by {@code GET /api/v1/checkout/summary}.
 * Provides a full price breakdown including optional coupon discount.
 * Maps to the {@code CheckoutSummary} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class CheckoutSummaryResponse {

    private final List<CartItemResponse> items;
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private final BigDecimal deliveryCharge;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;

    /** Applied coupon details — {@code null} when no coupon is active. */
    private final CouponSummaryResponse coupon;
}
