package com.bookstore.dto.response;

import com.bookstore.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Place-order confirmation returned by {@code POST /api/v1/checkout/place-order}.
 * Maps to the {@code OrderPlaced} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class OrderPlacedResponse {

    private final Long orderId;
    private final String orderNumber;
    private final OrderStatus status;
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private final BigDecimal deliveryCharge;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;
    private final LocalDate estimatedDeliveryDate;
    private final List<OrderItemResponse> items;
}
