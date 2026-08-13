package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response returned after adding a book to the wishlist.
 * Used as the {@code data} payload for {@code POST /api/v1/wishlist/items}.
 * Maps to the {@code WishlistItemCreated} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class WishlistItemCreatedResponse {

    private final Long wishlistItemId;
    private final Long bookId;
    private final Instant addedAt;
}
