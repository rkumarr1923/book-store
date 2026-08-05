package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.HomePageResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for home page sections.
 * All endpoints are public; authenticated requests receive personalised content.
 */
@Tag(name = "Home", description = "Home page sections: Recommended, Bestsellers, New Launches")
@RestController
@RequestMapping(AppConstants.Api.HOME_PATH)
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @SecurityRequirements
    @Operation(summary = "Full home page",
               description = "Returns all three home sections in a single call. Recommended is personalised for authenticated users.")
    @GetMapping
    public ResponseEntity<ApiResponse<HomePageResponse>> homePage(
            @AuthenticationPrincipal UserPrincipal principal) {

        Long userId = (principal != null) ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                homeService.getHomePage(userId, AppConstants.Pagination.HOME_SECTION_LIMIT)));
    }

    @SecurityRequirements
    @Operation(summary = "Recommended books")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> recommended(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = (principal != null) ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(homeService.getRecommended(userId, limit)));
    }

    @SecurityRequirements
    @Operation(summary = "Bestsellers")
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> bestsellers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(homeService.getBestsellers(limit)));
    }

    @SecurityRequirements
    @Operation(summary = "New Launches")
    @GetMapping("/new-launches")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> newLaunches(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(homeService.getNewLaunches(limit)));
    }
}
