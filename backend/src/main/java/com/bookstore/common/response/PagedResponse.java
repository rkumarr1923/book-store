package com.bookstore.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated response wrapper used for list endpoints.
 * Mirrors Spring Data's {@code Page} structure to keep the API contract
 * independent of the persistence layer.
 *
 * @param <T> the type of items in the page
 */
@Getter
@Builder
public class PagedResponse<T> {

    /** Items for the current page. */
    private final List<T> content;

    /** Zero-based current page index. */
    private final int page;

    /** Number of items requested per page. */
    private final int size;

    /** Total number of matching elements across all pages. */
    private final long totalElements;

    /** Total number of pages. */
    private final int totalPages;

    /** True if this is the last page. */
    private final boolean last;
}
