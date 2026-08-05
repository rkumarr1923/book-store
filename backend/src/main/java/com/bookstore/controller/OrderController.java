package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.dto.response.BuyAgainResponse;
import com.bookstore.dto.response.OrderDetailResponse;
import com.bookstore.dto.response.OrderSummaryResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order history, detail, and buy-again.
 * All endpoints require authentication.
 */
@Tag(name = "Orders", description = "Order history, detail, and buy again")
@RestController
@RequestMapping(AppConstants.Api.ORDERS_PATH)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Order history")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryResponse>>> listOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(required = false)    String status) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.listOrders(principal.getId(), page, size, status)));
    }

    @Operation(summary = "Order detail")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getOrderDetail(principal.getId(), orderId)));
    }

    @Operation(summary = "Buy Again — re-add past order items to cart")
    @PostMapping("/{orderId}/buy-again")
    public ResponseEntity<ApiResponse<BuyAgainResponse>> buyAgain(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        BuyAgainResponse result = orderService.buyAgain(principal.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok(result,
                result.getItemsAdded() + " item(s) added to cart"));
    }
}
