package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response for {@code POST /api/v1/wishlist/items/{wishlistItemId}/move-to-cart}.
 */
@Getter
@Builder
public class MoveToCartResponse {

    private final Long cartItemId;
    private final Long bookId;
}
