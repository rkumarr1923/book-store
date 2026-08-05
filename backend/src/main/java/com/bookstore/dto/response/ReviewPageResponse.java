package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated review response returned by {@code GET /api/v1/books/{bookId}/reviews}.
 * Includes aggregate statistics (average rating, total count) alongside
 * the paginated review list.
 * Maps to the {@code PagedReviews} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class ReviewPageResponse {

    private final Double averageRating;
    private final long reviewCount;
    private final List<ReviewResponse> content;

    /** Zero-based current page index. */
    private final int page;

    /** Items per page. */
    private final int size;

    /** Total matching reviews across all pages. */
    private final long totalElements;

    /** Total number of pages. */
    private final int totalPages;

    /** {@code true} if this is the last page. */
    private final boolean last;
}
