package com.bookstore.dto.response;

import com.bookstore.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Summary entry returned in the order history list.
 * Used by {@code GET /api/v1/orders}.
 * Maps to the {@code OrderSummary} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class OrderSummaryResponse {

    private final Long id;
    private final String orderNumber;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final int itemCount;
    private final Instant placedAt;
    private final LocalDate estimatedDeliveryDate;
    private final List<OrderItemResponse> items;
}
