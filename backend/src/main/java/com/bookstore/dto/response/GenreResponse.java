package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight genre DTO used inside book and catalogue responses.
 * Used by {@code GET /api/v1/genres} and embedded in book responses.
 */
@Getter
@Builder
public class GenreResponse {

    private final Long id;
    private final String name;
    private final String slug;
}
