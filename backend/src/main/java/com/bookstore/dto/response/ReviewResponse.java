package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Single review entry returned by the reviews listing endpoint and after
 * submitting a new review.
 * Maps to the {@code Review} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class ReviewResponse {

    private final Long id;
    private final Long userId;
    private final String userName;
    private final Integer rating;
    private final String reviewText;
    private final Instant createdAt;
}
