package com.bookstore.domain.enums;

/**
 * Payment method chosen by the customer.
 * Stored as a STRING in the database column {@code payment_method}.
 */
public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    WALLET
}
