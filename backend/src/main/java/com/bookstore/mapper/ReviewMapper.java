package com.bookstore.mapper;

import com.bookstore.domain.Review;
import com.bookstore.dto.request.CreateReviewRequest;
import com.bookstore.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Review} entity ↔ review DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code userName} in the response is composed from
 *       {@code review.user.firstName + " " + review.user.lastName}.
 *       MapStruct cannot derive this automatically, so it is ignored here;
 *       the service populates it.</li>
 *   <li>{@code userId} in the response maps from {@code review.user.id}.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // ── Entity → Response ─────────────────────────────────────────────────────

    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "userName", ignore = true)
    ReviewResponse toResponse(Review review);

    // ── Request → Entity ──────────────────────────────────────────────────────

    /**
     * Create a new {@link Review} from a review creation request.
     * {@code book} and {@code user} associations are set by the service layer.
     * {@code id} and {@code createdAt} are managed by the persistence layer and
     * are not settable via the builder; they are excluded because
     * {@link CreateReviewRequest} has no such fields.
     */
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    Review toEntity(CreateReviewRequest request);
}
