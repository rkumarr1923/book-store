package com.bookstore.dto.response;

import com.bookstore.common.response.PagedResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Full author profile DTO returned by {@code GET /api/v1/authors/{authorId}}.
 * Maps to the {@code AuthorDetail} schema in the OpenAPI contract.
 *
 * <p>{@code isFollowed} is {@code false} for unauthenticated requests and reflects
 * the actual follow status for authenticated users.
 */
@Getter
@Builder
public class AuthorDetailResponse {

    private final Long id;
    private final String name;
    private final String biography;
    private final String profileImageUrl;

    /** Follow status of the requesting user. {@code false} for guests. */
    private final boolean isFollowed;

    /** Paginated list of the author's active books. */
    private final PagedResponse<BookSummaryResponse> books;
}
