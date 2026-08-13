package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.dto.request.UpdateProfileRequest;
import com.bookstore.dto.request.AddressRequest;
import com.bookstore.dto.response.AddressResponse;
import com.bookstore.dto.response.FollowedAuthorResponse;
import com.bookstore.dto.response.UserProfileResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.AddressService;
import com.bookstore.service.AuthorService;
import com.bookstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user profile, saved addresses, and followed-authors list.
 * All endpoints require authentication.
 */
@Tag(name = "Users", description = "Profile, addresses, and followed authors")
@RestController
@RequestMapping(AppConstants.Api.USERS_PATH)
@RequiredArgsConstructor
public class UserController {

    private final UserService    userService;
    private final AddressService addressService;
    private final AuthorService  authorService;

    // ── Profile ────────────────────────────────────────────────────────────────

    @Operation(summary = "Get own profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(principal.getId())));
    }

    @Operation(summary = "Update own profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.updateProfile(principal.getId(), request),
                "Profile updated successfully"));
    }

    // ── Addresses ──────────────────────────────────────────────────────────────

    @Operation(summary = "List saved addresses")
    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> listAddresses(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                addressService.listAddresses(principal.getId())));
    }

    @Operation(summary = "Add a saved address")
    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                addressService.addAddress(principal.getId(), request),
                "Address added successfully"));
    }

    @Operation(summary = "Update a saved address")
    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                addressService.updateAddress(principal.getId(), addressId, request),
                "Address updated successfully"));
    }

    @Operation(summary = "Delete a saved address")
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {
        addressService.deleteAddress(principal.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set default address")
    @PatchMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(ApiResponse.ok(
                addressService.setDefault(principal.getId(), addressId),
                "Default address updated"));
    }

    // ── Followed Authors (My Writers) ──────────────────────────────────────────

    @Operation(summary = "List followed authors (My Writers)")
    @GetMapping("/me/followed-authors")
    public ResponseEntity<ApiResponse<PagedResponse<FollowedAuthorResponse>>> followedAuthors(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                authorService.getFollowedAuthors(principal.getId(), page, size)));
    }
}
