package com.bookstore.mapper;

import com.bookstore.domain.Book;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Book} entity → book response DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code shortDescription} in the summary is derived from {@code book.description}
 *       by the service (truncation logic). It is ignored here so the service can set it.</li>
 *   <li>{@code averageRating} and {@code reviewCount} require a separate aggregate query
 *       in the service layer and are therefore ignored in both mappings.</li>
 *   <li>{@code estimatedDeliveryDate} is computed by {@link com.bookstore.common.util.DeliveryDateCalculator}
 *       and set by the service layer.</li>
 * </ul>
 *
 * <p>Nested mappers ({@link GenreMapper}, {@link AuthorMapper}, {@link PublisherMapper})
 * are referenced via the {@code uses} attribute so MapStruct can delegate
 * {@code genres}, {@code author}, and {@code publisher} collection/object conversions.
 */
@Mapper(
    componentModel = "spring",
    uses = { GenreMapper.class, AuthorMapper.class, PublisherMapper.class }
)
public interface BookMapper {

    // ── Entity → BookSummaryResponse ──────────────────────────────────────────

    /**
     * Map a {@link Book} to its lightweight summary card.
     * Runtime-computed fields are ignored; service sets them after mapping.
     */
    @Mapping(target = "shortDescription",      ignore = true)
    @Mapping(target = "estimatedDeliveryDate", ignore = true)
    @Mapping(target = "averageRating",         ignore = true)
    BookSummaryResponse toSummaryResponse(Book book);

    // ── Entity → BookDetailResponse ───────────────────────────────────────────

    /**
     * Map a {@link Book} to its full detail response.
     * Runtime-computed fields are ignored; service sets them after mapping.
     * The nested {@code author} field maps to {@link com.bookstore.dto.response.AuthorDetailResponse}
     * via {@link AuthorMapper#toDetailResponse(com.bookstore.domain.Author)}.
     */
    @Mapping(target = "estimatedDeliveryDate", ignore = true)
    @Mapping(target = "averageRating",         ignore = true)
    @Mapping(target = "reviewCount",           ignore = true)
    BookDetailResponse toDetailResponse(Book book);
}
