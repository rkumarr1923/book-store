package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.PlaceOrderRequest;
import com.bookstore.dto.request.ValidateCouponRequest;
import com.bookstore.dto.response.CheckoutSummaryResponse;
import com.bookstore.dto.response.OrderPlacedResponse;
import com.bookstore.dto.response.ValidateCouponResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for checkout: summary, coupon validation, and order placement.
 * All endpoints require authentication.
 */
@Tag(name = "Checkout", description = "Checkout flow: summary, coupon validation, place order")
@RestController
@RequestMapping(AppConstants.Api.CHECKOUT_PATH)
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Operation(summary = "Get checkout summary")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CheckoutSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String couponCode) {
        return ResponseEntity.ok(ApiResponse.ok(
                checkoutService.getSummary(principal.getId(), couponCode)));
    }

    @Operation(summary = "Validate coupon code")
    @PostMapping("/validate-coupon")
    public ResponseEntity<ApiResponse<ValidateCouponResponse>> validateCoupon(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ValidateCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                checkoutService.validateCoupon(principal.getId(), request),
                "Coupon applied successfully"));
    }

    @Operation(summary = "Place order")
    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse<OrderPlacedResponse>> placeOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                checkoutService.placeOrder(principal.getId(), request),
                "Order placed successfully. Proceed to payment."));
    }
}
