package com.bookstore.domain.enums;

/**
 * Status of a payment transaction.
 * Stored as a STRING in the database column {@code status}.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
