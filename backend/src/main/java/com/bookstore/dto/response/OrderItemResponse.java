package com.bookstore.dto.response;

import com.bookstore.domain.enums.BookFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * A single line item in a placed or confirmed order.
 * Captures the price snapshot at order time.
 * Used inside {@link OrderPlacedResponse}, {@link OrderSummaryResponse},
 * {@link OrderDetailResponse}, and {@link PaymentResultResponse}.
 * Maps to the {@code OrderItem} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class OrderItemResponse {

    private final Long id;
    private final Long bookId;
    private final String title;
    private final String coverImageUrl;
    private final BookFormat format;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;
}
