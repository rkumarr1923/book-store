package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Minimal author summary embedded in book cards and cart items.
 * Used wherever only the author's ID and name are needed (e.g. {@link BookSummaryResponse}).
 */
@Getter
@Builder
public class AuthorSummaryResponse {

    private final Long id;
    private final String name;
}
