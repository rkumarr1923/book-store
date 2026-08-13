package com.bookstore.dto.response;

import com.bookstore.domain.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Coupon validation result returned by
 * {@code POST /api/v1/checkout/validate-coupon}.
 * Maps to the {@code CouponValidateResponse} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class ValidateCouponResponse {

    private final String couponCode;
    private final DiscountType discountType;
    private final BigDecimal discountValue;

    /** Computed INR discount amount applied to the current cart subtotal. */
    private final BigDecimal discountAmount;

    /** Always {@code true} in the success response (invalid coupons throw an exception). */
    private final boolean valid;
}
