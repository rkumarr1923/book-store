package com.bookstore.mapper;

import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.dto.response.CartItemCreatedResponse;
import com.bookstore.dto.response.CartItemResponse;
import com.bookstore.dto.response.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Cart} / {@link CartItem} entities → cart response DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code cartId} in {@link CartResponse} maps from {@code cart.id}.</li>
 *   <li>{@code updatedAt} in {@link CartResponse} maps from {@code cart.updatedAt}.</li>
 *   <li>{@code itemCount} is computed by the service and ignored here.</li>
 *   <li>{@code subtotal} is computed by the service and ignored here.</li>
 *   <li>{@code cartItemId} in the item responses maps from {@code cartItem.id}.</li>
 *   <li>{@code bookId} maps from {@code cartItem.book.id}.</li>
 *   <li>{@code title} maps from {@code cartItem.book.title}.</li>
 *   <li>{@code coverImageUrl} maps from {@code cartItem.book.coverImageUrl}.</li>
 *   <li>{@code author} delegates to {@link AuthorMapper} via the {@code uses} attribute.</li>
 *   <li>{@code format} maps from {@code cartItem.book.format}.</li>
 *   <li>{@code genres} delegates to {@link GenreMapper} via the {@code uses} attribute.</li>
 *   <li>{@code unitPrice} maps from {@code cartItem.book.price}.</li>
 *   <li>{@code lineTotal} and {@code estimatedDeliveryDate} are computed by the service.</li>
 * </ul>
 */
@Mapper(
    componentModel = "spring",
    uses = { AuthorMapper.class, GenreMapper.class }
)
public interface CartMapper {

    // ── CartItem → CartItemResponse ───────────────────────────────────────────

    @Mapping(target = "cartItemId",            source = "id")
    @Mapping(target = "bookId",                source = "book.id")
    @Mapping(target = "title",                 source = "book.title")
    @Mapping(target = "coverImageUrl",         source = "book.coverImageUrl")
    @Mapping(target = "author",                source = "book.author")
    @Mapping(target = "format",                source = "book.format")
    @Mapping(target = "genres",                source = "book.genres")
    @Mapping(target = "unitPrice",             source = "book.price")
    @Mapping(target = "lineTotal",             ignore = true)
    @Mapping(target = "estimatedDeliveryDate", ignore = true)
    CartItemResponse toItemResponse(CartItem cartItem);

    // ── CartItem → CartItemCreatedResponse ────────────────────────────────────

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "bookId",     source = "book.id")
    @Mapping(target = "lineTotal",  ignore = true)
    CartItemCreatedResponse toCreatedResponse(CartItem cartItem);

    // ── Cart → CartResponse ───────────────────────────────────────────────────

    @Mapping(target = "cartId",    source = "id")
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "subtotal",  ignore = true)
    CartResponse toCartResponse(Cart cart);
}
