package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Followed-author entry in the "My Writers" list.
 * Returned inside the paginated response for
 * {@code GET /api/v1/users/me/followed-authors}.
 * Maps to the {@code FollowedAuthor} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class FollowedAuthorResponse {

    private final Long id;
    private final String name;
    private final String biography;
    private final String profileImageUrl;

    /** Timestamp when the user followed this author. */
    private final Instant followedAt;

    /** Up to 3 most recent books by this author. */
    private final List<BookSummaryResponse> recentBooks;
}
