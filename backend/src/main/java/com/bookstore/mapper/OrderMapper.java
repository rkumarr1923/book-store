package com.bookstore.mapper;

import com.bookstore.domain.Order;
import com.bookstore.domain.OrderItem;
import com.bookstore.dto.response.DeliveryAddressSnapshotResponse;
import com.bookstore.dto.response.OrderDetailResponse;
import com.bookstore.dto.response.OrderItemResponse;
import com.bookstore.dto.response.OrderPlacedResponse;
import com.bookstore.dto.response.OrderSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Order} / {@link OrderItem} entities → order response DTOs.
 *
 * <p>Field-name notes for {@link OrderSummaryResponse}:
 * <ul>
 *   <li>{@code itemCount} is computed ({@code order.items.size()}) — ignored, service sets it.</li>
 * </ul>
 *
 * <p>Field-name notes for {@link OrderDetailResponse}:
 * <ul>
 *   <li>{@code couponCode} maps from {@code order.coupon.code} (nullable).</li>
 *   <li>{@code deliveryAddress} is built from {@code order.deliveryAddressSnapshot} (a
 *       {@code Map<String, Object>}) by the service layer. Ignored here.</li>
 *   <li>{@code payment} is mapped via {@link PaymentMapper} — ignored here, service sets it.</li>
 * </ul>
 *
 * <p>Field-name notes for {@link OrderItemResponse}:
 * <ul>
 *   <li>All fields come directly from snapshot columns on the entity — no cross-entity lookups.</li>
 *   <li>{@code bookId} maps from {@code orderItem.book.id}.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ── OrderItem → OrderItemResponse ─────────────────────────────────────────

    @Mapping(target = "bookId", source = "book.id")
    OrderItemResponse toItemResponse(OrderItem orderItem);

    // ── Order → OrderSummaryResponse (list endpoint) ──────────────────────────

    @Mapping(target = "itemCount", ignore = true)
    OrderSummaryResponse toSummaryResponse(Order order);

    // ── Order → OrderDetailResponse (detail endpoint) ─────────────────────────

    @Mapping(target = "couponCode",       source = "coupon.code")
    @Mapping(target = "deliveryAddress",  ignore = true)
    @Mapping(target = "payment",          ignore = true)
    OrderDetailResponse toDetailResponse(Order order);

    // ── Order → OrderPlacedResponse (place-order endpoint) ────────────────────

    @Mapping(target = "orderId", source = "id")
    OrderPlacedResponse toPlacedResponse(Order order);

    // ── Delivery address snapshot helper ──────────────────────────────────────

    /**
     * Build a {@link DeliveryAddressSnapshotResponse} from the raw {@code Map<String, Object>}
     * stored in the {@code delivery_address_snapshot} JSON column.
     * MapStruct cannot auto-derive this from a map; the service layer calls this
     * by converting the map to a record/object before invoking this method,
     * or constructs the snapshot response directly. This interface-level method
     * is a no-op bridge kept here for documentation clarity.
     *
     * <p>The service layer should construct {@link DeliveryAddressSnapshotResponse}
     * directly using {@code .builder()} from the map entries.
     */
    default DeliveryAddressSnapshotResponse toSnapshotResponse(
            java.util.Map<String, Object> snapshot) {
        if (snapshot == null) return null;
        return DeliveryAddressSnapshotResponse.builder()
                .firstName(  (String) snapshot.getOrDefault("firstName",   null))
                .lastName(   (String) snapshot.getOrDefault("lastName",    null))
                .addressLine1((String) snapshot.getOrDefault("addressLine1", null))
                .addressLine2((String) snapshot.getOrDefault("addressLine2", null))
                .city(       (String) snapshot.getOrDefault("city",        null))
                .state(      (String) snapshot.getOrDefault("state",       null))
                .country(    (String) snapshot.getOrDefault("country",     null))
                .pinCode(    (String) snapshot.getOrDefault("pinCode",     null))
                .build();
    }
}
