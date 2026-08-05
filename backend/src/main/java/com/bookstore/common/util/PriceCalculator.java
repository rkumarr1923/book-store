package com.bookstore.common.util;

import com.bookstore.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for computing order pricing components.
 * All monetary arithmetic uses {@link BigDecimal} to avoid floating-point errors.
 */
@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final AppProperties appProperties;

    /**
     * Computes the tax amount for a given subtotal using the configured tax rate.
     *
     * @param subtotal order subtotal in INR
     * @return tax amount rounded to 2 decimal places
     */
    public BigDecimal computeTax(BigDecimal subtotal) {
        BigDecimal rate = BigDecimal.valueOf(appProperties.getTax().getRate());
        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the discount amount for a FIXED coupon.
     * The discount is capped at the subtotal (cannot produce a negative total).
     *
     * @param subtotal      order subtotal
     * @param discountValue flat INR discount
     * @return actual discount applied
     */
    public BigDecimal computeFixedDiscount(BigDecimal subtotal, BigDecimal discountValue) {
        return discountValue.min(subtotal);
    }

    /**
     * Computes the discount amount for a PERCENTAGE coupon.
     * Percentage is expressed as a whole number (e.g. 10 = 10%).
     *
     * @param subtotal   order subtotal
     * @param percentage discount percentage (0–100)
     * @return actual discount applied, rounded to 2 decimal places
     */
    public BigDecimal computePercentageDiscount(BigDecimal subtotal, BigDecimal percentage) {
        return subtotal.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the final order total.
     *
     * @param subtotal       sum of all line totals
     * @param taxAmount      computed tax
     * @param deliveryCharge delivery fee (0 for free)
     * @param discountAmount applied coupon discount
     * @return total amount payable (minimum 0)
     */
    public BigDecimal computeTotal(BigDecimal subtotal,
                                   BigDecimal taxAmount,
                                   BigDecimal deliveryCharge,
                                   BigDecimal discountAmount) {
        BigDecimal total = subtotal
                .add(taxAmount)
                .add(deliveryCharge)
                .subtract(discountAmount);
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
