package com.bookstore.domain.enums;

/**
 * Lifecycle status of a customer order.
 * Stored as a STRING in the database column {@code status}.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
