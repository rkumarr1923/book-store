package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.AddToCartRequest;
import com.bookstore.dto.request.UpdateCartItemRequest;
import com.bookstore.dto.response.CartItemCreatedResponse;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart.
 * All endpoints require authentication.
 */
@Tag(name = "Cart", description = "Shopping cart management")
@RestController
@RequestMapping(AppConstants.Api.CART_PATH)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "View cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(principal.getId())));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemCreatedResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                cartService.addItem(principal.getId(), request), "Book added to cart"));
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemCreatedResponse>> updateItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                cartService.updateItem(principal.getId(), cartItemId, request),
                "Cart item updated"));
    }

    @Operation(summary = "Remove cart item")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long cartItemId) {
        cartService.removeItem(principal.getId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
