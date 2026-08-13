package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.response.GenreResponse;
import com.bookstore.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for genre list endpoint.
 * GET /api/v1/genres — public, no JWT required.
 */
@Tag(name = "Genres", description = "Genre catalogue for sidebar navigation")
@RestController
@RequestMapping(AppConstants.Api.GENRES_PATH)
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @SecurityRequirements
    @Operation(summary = "List all genres", description = "Returns all book genres ordered alphabetically.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> listGenres() {
        return ResponseEntity.ok(ApiResponse.ok(genreService.findAll()));
    }
}
