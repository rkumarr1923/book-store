package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/checkout/validate-coupon}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 1, max = 50, message = "Coupon code must be between 1 and 50 characters")
    private String couponCode;
}
