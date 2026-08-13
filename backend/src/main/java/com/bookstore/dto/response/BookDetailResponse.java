package com.bookstore.dto.response;

import com.bookstore.domain.enums.BookFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Full book detail DTO returned by {@code GET /api/v1/books/{bookId}}.
 * Extends the summary fields with description, ISBN, language, review stats,
 * full author bio, and publisher information.
 * Maps to the {@code BookDetail} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class BookDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String coverImageUrl;
    private final String isbn;
    private final String language;
    private final BookFormat format;
    private final BigDecimal price;
    private final int copiesSold;
    private final LocalDate publishedDate;
    private final LocalDate estimatedDeliveryDate;
    private final Double averageRating;
    private final long reviewCount;
    private final List<GenreResponse> genres;

    /** Full author profile (includes biography and profile image). */
    private final AuthorDetailResponse author;

    /** Publisher info (nullable — not all books have a publisher on record). */
    private final PublisherResponse publisher;
}
