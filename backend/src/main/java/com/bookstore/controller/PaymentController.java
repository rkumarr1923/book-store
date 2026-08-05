package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.PaymentRequest;
import com.bookstore.dto.response.PaymentRecordResponse;
import com.bookstore.dto.response.PaymentResultResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment processing (Phase 1 mock).
 * All endpoints require authentication.
 */
@Tag(name = "Payments", description = "Payment processing (Phase 1 mock implementation)")
@RestController
@RequestMapping(AppConstants.Api.PAYMENTS_PATH)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process payment",
               description = "Phase 1 mock: all valid requests succeed. Card data is never persisted.")
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResultResponse>> processPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.processPayment(principal.getId(), request),
                "Payment successful. Your order has been confirmed."));
    }

    @Operation(summary = "Get payment record for an order")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PaymentRecordResponse>> getPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.getPayment(principal.getId(), orderId)));
    }
}
