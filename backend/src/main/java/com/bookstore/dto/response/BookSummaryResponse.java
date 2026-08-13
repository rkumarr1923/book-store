package com.bookstore.dto.response;

import com.bookstore.domain.enums.BookFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight book card DTO used on home page sections, catalogue listing, search results,
 * and the "related books" sidebar.
 * Maps to the {@code BookSummary} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class BookSummaryResponse {

    private final Long id;
    private final String title;
    private final AuthorSummaryResponse author;
    private final String coverImageUrl;

    /** First sentence or truncated excerpt of the book's description. */
    private final String shortDescription;

    private final BookFormat format;
    private final List<GenreResponse> genres;
    private final BigDecimal price;
    private final LocalDate estimatedDeliveryDate;
    private final Double averageRating;
    private final int copiesSold;
}
