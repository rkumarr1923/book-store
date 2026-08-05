package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Shopping cart response returned by {@code GET /api/v1/cart}.
 * Maps to the {@code Cart} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class CartResponse {

    private final Long cartId;
    private final int itemCount;
    private final List<CartItemResponse> items;
    private final BigDecimal subtotal;
    private final Instant updatedAt;
}
