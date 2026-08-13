package com.bookstore.domain.enums;

/**
 * Type of discount applied by a coupon.
 * Stored as a STRING in the database column {@code discount_type}.
 */
public enum DiscountType {
    /** Flat INR amount deducted from the order subtotal. */
    FIXED,
    /** Percentage deducted from the order subtotal. */
    PERCENTAGE
}
