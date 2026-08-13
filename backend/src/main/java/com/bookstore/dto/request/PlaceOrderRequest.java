package com.bookstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/checkout/place-order}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @Size(max = 50)
    private String couponCode;

    @NotNull(message = "Delivery address is required")
    @Valid
    private DeliveryAddressRequest deliveryAddress;
}
