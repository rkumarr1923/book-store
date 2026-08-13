package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response for {@code POST /api/v1/authors/{authorId}/follow}
 * and {@code DELETE /api/v1/authors/{authorId}/follow}.
 */
@Getter
@Builder
public class AuthorFollowResponse {

    private final Long authorId;

    /** {@code true} after a follow action; {@code false} after an unfollow action. */
    private final boolean followed;
}
