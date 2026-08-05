package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * A single entry in the paginated wishlist.
 * Used inside the list returned by {@code GET /api/v1/wishlist}.
 * Maps to the {@code WishlistItem} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class WishlistItemResponse {

    private final Long wishlistItemId;
    private final Instant addedAt;
    private final BookSummaryResponse book;
}
