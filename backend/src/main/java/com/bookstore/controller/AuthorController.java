package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.response.AuthorDetailResponse;
import com.bookstore.dto.response.AuthorFollowResponse;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for author profiles and follow/unfollow.
 */
@Tag(name = "Authors", description = "Author profiles, follow and unfollow")
@RestController
@RequestMapping(AppConstants.Api.AUTHORS_PATH)
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @SecurityRequirements
    @Operation(summary = "Get author profile")
    @GetMapping("/{authorId}")
    public ResponseEntity<ApiResponse<AuthorDetailResponse>> getAuthor(
            @PathVariable Long authorId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = (principal != null) ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                authorService.findById(authorId, userId, page, size)));
    }

    @Operation(summary = "Follow an author")
    @PostMapping("/{authorId}/follow")
    public ResponseEntity<ApiResponse<AuthorFollowResponse>> follow(
            @PathVariable Long authorId,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.ok(
                authorService.follow(authorId, principal.getId()),
                "Author followed successfully"));
    }

    @Operation(summary = "Unfollow an author")
    @DeleteMapping("/{authorId}/follow")
    public ResponseEntity<ApiResponse<AuthorFollowResponse>> unfollow(
            @PathVariable Long authorId,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.ok(
                authorService.unfollow(authorId, principal.getId()),
                "Author unfollowed successfully"));
    }
}
