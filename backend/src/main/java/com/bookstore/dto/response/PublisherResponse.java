package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Publisher summary embedded in the book detail response.
 * Used by {@link BookDetailResponse}.
 */
@Getter
@Builder
public class PublisherResponse {

    private final Long id;
    private final String name;
    private final String website;
}
