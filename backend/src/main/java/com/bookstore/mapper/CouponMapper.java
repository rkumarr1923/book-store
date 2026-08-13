package com.bookstore.mapper;

import com.bookstore.domain.Coupon;
import com.bookstore.dto.response.CouponSummaryResponse;
import com.bookstore.dto.response.ValidateCouponResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Coupon} entity → coupon response DTOs.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>{@code code} in {@link CouponSummaryResponse} maps from {@code coupon.code}
 *       (same name — auto-mapped).</li>
 *   <li>{@code couponCode} in {@link ValidateCouponResponse} maps from {@code coupon.code}.</li>
 *   <li>{@code discountAmount} in {@link ValidateCouponResponse} is the computed INR saving
 *       for the current cart. It is ignored here; the service sets it.</li>
 *   <li>{@code valid} is always {@code true} in successful responses. It is ignored here;
 *       the service sets it.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface CouponMapper {

    // ── Entity → CouponSummaryResponse (embedded in checkout summary) ─────────

    CouponSummaryResponse toSummaryResponse(Coupon coupon);

    // ── Entity → ValidateCouponResponse (validate-coupon endpoint) ────────────

    @Mapping(target = "couponCode",      source = "code")
    @Mapping(target = "discountAmount",  ignore = true)
    @Mapping(target = "valid",           ignore = true)
    ValidateCouponResponse toValidateResponse(Coupon coupon);
}
