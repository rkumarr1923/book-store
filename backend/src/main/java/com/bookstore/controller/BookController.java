package com.bookstore.controller;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for the book catalogue.
 * All endpoints are public (no JWT required).
 */
@Tag(name = "Books", description = "Book catalogue, search, detail, and related reads")
@RestController
@RequestMapping(AppConstants.Api.BOOKS_PATH)
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @SecurityRequirements
    @Operation(summary = "Browse catalogue", description = "Paginated, filterable, and searchable book listing.")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookSummaryResponse>>> listBooks(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    String genreSlug,
            @RequestParam(required = false)    String language,
            @RequestParam(required = false)    String format,
            @RequestParam(required = false)    BigDecimal minPrice,
            @RequestParam(required = false)    BigDecimal maxPrice,
            @RequestParam(required = false)    String sortBy) {

        return ResponseEntity.ok(ApiResponse.ok(
                bookService.findAll(page, size, search, genreSlug, language, format,
                        minPrice, maxPrice, sortBy)));
    }

    @SecurityRequirements
    @Operation(summary = "Get book detail")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.ok(bookService.findById(bookId)));
    }

    @SecurityRequirements
    @Operation(summary = "Get related books", description = "Returns books related by shared genres.")
    @GetMapping("/{bookId}/related")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> getRelated(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(bookService.findRelated(bookId, limit)));
    }
}
