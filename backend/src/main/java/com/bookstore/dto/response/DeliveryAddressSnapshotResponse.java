package com.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Delivery address as stored in the {@code delivery_address_snapshot} JSON column.
 * Used inside {@link OrderDetailResponse}.
 * Maps to the {@code DeliveryAddressSnapshot} schema in the OpenAPI contract.
 */
@Getter
@Builder
public class DeliveryAddressSnapshotResponse {

    private final String firstName;
    private final String lastName;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String country;
    private final String pinCode;
}
