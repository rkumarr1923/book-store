package com.bookstore.mapper;

import com.bookstore.domain.WishlistItem;
import com.bookstore.dto.response.WishlistItemCreatedResponse;
import com.bookstore.dto.response.WishlistItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link WishlistItem} entity → wishlist response DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code wishlistItemId} in the response maps from the entity's {@code id}.</li>
 *   <li>{@code addedAt} in both responses maps from the entity's {@code createdAt}.</li>
 *   <li>{@code bookId} in {@link WishlistItemCreatedResponse} maps from {@code wishlistItem.book.id}.</li>
 * </ul>
 *
 * <p>The nested {@code book} field in {@link WishlistItemResponse} delegates to
 * {@link BookMapper#toSummaryResponse} via the {@code uses} attribute.
 */
@Mapper(
    componentModel = "spring",
    uses = { BookMapper.class }
)
public interface WishlistMapper {

    // ── Entity → WishlistItemResponse (paginated listing) ─────────────────────

    @Mapping(target = "wishlistItemId", source = "id")
    @Mapping(target = "addedAt",        source = "createdAt")
    WishlistItemResponse toItemResponse(WishlistItem wishlistItem);

    // ── Entity → WishlistItemCreatedResponse (after POST /wishlist/items) ─────

    @Mapping(target = "wishlistItemId", source = "id")
    @Mapping(target = "bookId",         source = "book.id")
    @Mapping(target = "addedAt",        source = "createdAt")
    WishlistItemCreatedResponse toCreatedResponse(WishlistItem wishlistItem);
}
