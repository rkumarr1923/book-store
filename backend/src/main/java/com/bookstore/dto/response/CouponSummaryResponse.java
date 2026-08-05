package com.bookstore.dto.response;

import com.bookstore.domain.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Applied coupon summary nested inside {@link CheckoutSummaryResponse}.
 * Present only when a valid coupon has been applied to the order.
 */
@Getter
@Builder
public class CouponSummaryResponse {

    private final String code;
    private final DiscountType discountType;
    private final BigDecimal discountValue;
}
