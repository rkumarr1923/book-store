package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.dto.request.AddToWishlistRequest;
import com.bookstore.dto.response.MoveToCartResponse;
import com.bookstore.dto.response.WishlistItemCreatedResponse;
import com.bookstore.dto.response.WishlistItemResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for wishlist management.
 * All endpoints require authentication.
 */
@Tag(name = "Wishlist", description = "Personal wishlist management")
@RestController
@RequestMapping(AppConstants.Api.WISHLIST_PATH)
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get wishlist")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WishlistItemResponse>>> getWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                wishlistService.listWishlist(principal.getId(), page, size)));
    }

    @Operation(summary = "Add book to wishlist")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistItemCreatedResponse>> addToWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToWishlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                wishlistService.addToWishlist(principal.getId(), request),
                "Book added to wishlist"));
    }

    @Operation(summary = "Remove item from wishlist")
    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long wishlistItemId) {
        wishlistService.removeFromWishlist(principal.getId(), wishlistItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Move wishlist item to cart")
    @PostMapping("/items/{wishlistItemId}/move-to-cart")
    public ResponseEntity<ApiResponse<MoveToCartResponse>> moveToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long wishlistItemId) {
        return ResponseEntity.ok(ApiResponse.ok(
                wishlistService.moveToCart(principal.getId(), wishlistItemId),
                "Book moved to cart"));
    }
}
