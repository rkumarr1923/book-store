package com.bookstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated and the request cannot be processed
 * even though it is syntactically valid.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Examples: expired coupon, order already paid, cart is empty.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
