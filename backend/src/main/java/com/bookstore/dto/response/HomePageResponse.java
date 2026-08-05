package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Home page data returned by {@code GET /api/v1/home}.
 * Aggregates all three curated sections in a single response.
 * Maps to the {@code HomePageData} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class HomePageResponse {

    private final List<BookSummaryResponse> recommended;
    private final List<BookSummaryResponse> bestsellers;
    private final List<BookSummaryResponse> newLaunches;
}
