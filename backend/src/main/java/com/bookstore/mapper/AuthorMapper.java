package com.bookstore.mapper;

import com.bookstore.domain.Author;
import com.bookstore.dto.response.AuthorDetailResponse;
import com.bookstore.dto.response.AuthorSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Author} entity → author response DTOs.
 *
 * <p>{@link AuthorDetailResponse} contains {@code isFollowed} and {@code books},
 * which are context-dependent values (requires the calling user's ID and a
 * separate book query). Those fields are ignored here; the service layer
 * populates them after mapping.
 */
@Mapper(componentModel = "spring")
public interface AuthorMapper {

    // ── Summary (embedded in book cards, cart items, etc.) ────────────────────

    AuthorSummaryResponse toSummaryResponse(Author author);

    // ── Full detail (author profile page) ─────────────────────────────────────

    /**
     * Map base author fields. {@code isFollowed} and {@code books} must be
     * set by the service layer because they require runtime context.
     */
    @Mapping(target = "isFollowed", ignore = true)
    @Mapping(target = "books",      ignore = true)
    AuthorDetailResponse toDetailResponse(Author author);
}
