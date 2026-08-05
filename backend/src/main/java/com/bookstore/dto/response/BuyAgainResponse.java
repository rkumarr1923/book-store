package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response for {@code POST /api/v1/orders/{orderId}/buy-again}.
 * Returns the updated cart after re-adding all past order items.
 * Maps to the {@code BuyAgainResult} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class BuyAgainResponse {

    private final Long cartId;
    private final int itemCount;
    private final int itemsAdded;
    private final List<CartItemResponse> items;
}
