package com.bookstore.mapper;

import com.bookstore.domain.Payment;
import com.bookstore.dto.response.PaymentRecordResponse;
import com.bookstore.dto.response.PaymentResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Payment} entity → payment response DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code paymentId} in the response maps from {@code payment.id}.</li>
 *   <li>{@code orderId} in the response maps from {@code payment.order.id}.</li>
 *   <li>{@code createdAt} maps from {@code payment.createdAt} (inherited from
 *       {@link com.bookstore.domain.BaseCreatedEntity}).</li>
 * </ul>
 *
 * <p>{@link PaymentResultResponse} includes the full {@code order} detail, which
 * requires a separate mapping by the service layer and is ignored here.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    // ── Entity → PaymentRecordResponse ───────────────────────────────────────

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "orderId",   source = "order.id")
    PaymentRecordResponse toRecordResponse(Payment payment);

    // ── Entity → PaymentResultResponse ───────────────────────────────────────

    /**
     * Map payment fields for the process-payment response.
     * {@code orderNumber} is sourced from the nested order.
     * The full {@code order} detail object is set by the service layer after mapping.
     */
    @Mapping(target = "paymentId",   source = "id")
    @Mapping(target = "orderId",     source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "order",       ignore = true)
    PaymentResultResponse toResultResponse(Payment payment);
}
