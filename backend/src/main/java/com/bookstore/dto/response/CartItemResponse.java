package com.bookstore.dto.response;

import com.bookstore.domain.enums.BookFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A single item inside the shopping cart.
 * Used in {@link CartResponse} and {@link CheckoutSummaryResponse}.
 * Maps to the {@code CartItem} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class CartItemResponse {

    private final Long cartItemId;
    private final Long bookId;
    private final String title;
    private final String coverImageUrl;
    private final AuthorSummaryResponse author;
    private final BookFormat format;
    private final List<GenreResponse> genres;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;
    private final LocalDate estimatedDeliveryDate;
}
