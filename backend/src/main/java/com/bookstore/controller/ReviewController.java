package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.CreateReviewRequest;
import com.bookstore.dto.response.ReviewPageResponse;
import com.bookstore.dto.response.ReviewResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for book reviews.
 * GET is public; POST requires authentication.
 */
@Tag(name = "Reviews", description = "Book reviews — list and submit")
@RestController
@RequestMapping(AppConstants.Api.BOOKS_PATH + "/{bookId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @SecurityRequirements
    @Operation(summary = "List reviews for a book")
    @GetMapping
    public ResponseEntity<ApiResponse<ReviewPageResponse>> listReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.listReviews(bookId, page, size)));
    }

    @Operation(summary = "Submit a review")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                reviewService.createReview(bookId, principal.getId(), request),
                "Review submitted successfully"));
    }
}
