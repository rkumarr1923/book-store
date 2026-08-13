package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Minimal response returned after adding or updating a cart item.
 * Used by {@code POST /api/v1/cart/items} and {@code PUT /api/v1/cart/items/{cartItemId}}.
 * Maps to the {@code CartItemCreated} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class CartItemCreatedResponse {

    private final Long cartItemId;
    private final Long bookId;
    private final int quantity;
    private final BigDecimal lineTotal;
}
