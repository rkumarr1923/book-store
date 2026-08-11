package com.bookstore.service;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.common.util.DeliveryDateCalculator;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.domain.enums.BookFormat;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BookSpecification;
import com.bookstore.repository.GenreRepository;
import com.bookstore.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for the book catalogue.
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository         bookRepository;
    private final GenreRepository        genreRepository;
    private final ReviewRepository       reviewRepository;
    private final BookMapper             bookMapper;
    private final DeliveryDateCalculator deliveryDateCalculator;

    // ── Catalogue ──────────────────────────────────────────────────────────────

    /**
     * Return a paginated, filtered, and searchable list of active books.
     *
     * All non-null, non-blank filter parameters are applied simultaneously using
     * a JPA {@link Specification}, eliminating the previous if/else chain that
     * only honoured the first matching filter category.
     *
     * Ratings are fetched in a single batch query for all books on the current
     * page, eliminating the previous N+1 per-book rating pattern.
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookSummaryResponse> findAll(
            int page, int size,
            String search,
            String genreSlug,
            String language,
            String format,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy) {

        size = Math.min(size, AppConstants.Pagination.MAX_PAGE_SIZE);
        Sort     sort     = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Resolve genre slug → ID (null when no genre filter or slug unknown)
        Long genreId = null;
        if (StringUtils.hasText(genreSlug) && !genreSlug.equalsIgnoreCase("all")) {
            genreId = genreRepository.findBySlug(genreSlug)
                    .map(Genre::getId)
                    .orElse(null);
        }

        // Resolve format string → enum (null when no format filter)
        BookFormat bookFormat = null;
        if (StringUtils.hasText(format)) {
            try {
                bookFormat = BookFormat.valueOf(format.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Unknown format value — treat as no format filter
            }
        }

        // Resolve language (null when 'All' or blank)
        String languageFilter = (StringUtils.hasText(language) && !language.equalsIgnoreCase("All"))
                ? language.trim()
                : null;

        // Build the specification from all active filters
        Specification<Book> spec = BookSpecification.of(
                StringUtils.hasText(search) ? search.trim() : null,
                genreId,
                languageFilter,
                bookFormat,
                minPrice,
                maxPrice
        );

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        // Batch-fetch ratings for every book on this page in one query
        List<Long> bookIds = bookPage.getContent().stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        Map<Long, Double> ratingMap = batchFetchRatings(bookIds);

        List<BookSummaryResponse> content = bookPage.getContent()
                .stream()
                .map(b -> toSummaryWithComputedFields(b, ratingMap.get(b.getId())))
                .collect(Collectors.toList());

        return PagedResponse.<BookSummaryResponse>builder()
                .content(content)
                .page(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .last(bookPage.isLast())
                .build();
    }

    // ── Book Detail ────────────────────────────────────────────────────────────

    /**
     * Return the full detail for a single active book.
     */
    @Transactional(readOnly = true)
    public BookDetailResponse findById(Long bookId) {
        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        BookDetailResponse response = bookMapper.toDetailResponse(book);

        Double avgRating   = reviewRepository.findAverageRatingByBookId(bookId);
        long   reviewCount = reviewRepository.countByBookId(bookId);
        LocalDate delivery = deliveryDateCalculator.calculate();

        return BookDetailResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .coverImageUrl(response.getCoverImageUrl())
                .isbn(response.getIsbn())
                .language(response.getLanguage())
                .format(response.getFormat())
                .price(response.getPrice())
                .copiesSold(response.getCopiesSold())
                .publishedDate(response.getPublishedDate())
                .estimatedDeliveryDate(delivery)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .genres(response.getGenres())
                .author(response.getAuthor())
                .publisher(response.getPublisher())
                .build();
    }

    // ── Related Books ──────────────────────────────────────────────────────────

    /**
     * Return books related to the specified book by shared genres.
     * Ratings are fetched in a single batch for all related books.
     */
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> findRelated(Long bookId, int limit) {
        limit = Math.min(limit, AppConstants.Pagination.MAX_RELATED_LIMIT);

        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        Pageable pageable = PageRequest.of(0, limit);

        // Primary: same author (ordered by published date desc)
        List<Book> related = new ArrayList<>(
                bookRepository.findRelatedByAuthor(book.getAuthor().getId(), bookId, pageable));

        // Secondary: same genres (fill up to limit)
        if (related.size() < limit) {
            List<Long> genreIds = book.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());

            if (!genreIds.isEmpty()) {
                int remaining = limit - related.size();
                Pageable remainingPageable = PageRequest.of(0, remaining);
                Set<Long> existingIds = new HashSet<>();
                existingIds.add(bookId);
                related.forEach(b -> existingIds.add(b.getId()));

                bookRepository.findRelatedByGenres(genreIds, bookId, remainingPageable)
                        .stream()
                        .filter(b -> !existingIds.contains(b.getId()))
                        .forEach(related::add);
            }
        }

        // Batch-fetch ratings for all related books in one query
        List<Long> relatedIds = related.stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        Map<Long, Double> ratingMap = batchFetchRatings(relatedIds);

        return related.stream()
                .limit(limit)
                .map(b -> toSummaryWithComputedFields(b, ratingMap.get(b.getId())))
                .collect(Collectors.toList());
    }

    // ── Package-level helpers (used by HomeService) ────────────────────────────

    /**
     * Map a book to summary DTO and set runtime-computed fields.
     * Fetches the rating individually — use only when a pre-built rating map is
     * not available (i.e. HomeService single-book mappings where batch is not
     * practical for small fixed lists).
     */
    public BookSummaryResponse toSummaryWithComputedFields(Book book) {
        Double avgRating = reviewRepository.findAverageRatingByBookId(book.getId());
        return toSummaryWithComputedFields(book, avgRating);
    }

    /**
     * Map a book to summary DTO with a pre-fetched average rating.
     * Pass {@code null} for {@code averageRating} when the book has no reviews.
     */
    public BookSummaryResponse toSummaryWithComputedFields(Book book, Double averageRating) {
        BookSummaryResponse base = bookMapper.toSummaryResponse(book);
        String shortDesc = truncate(book.getDescription(), 120);
        LocalDate delivery = deliveryDateCalculator.calculate();

        return BookSummaryResponse.builder()
                .id(base.getId())
                .title(base.getTitle())
                .author(base.getAuthor())
                .coverImageUrl(base.getCoverImageUrl())
                .shortDescription(shortDesc)
                .format(base.getFormat())
                .genres(base.getGenres())
                .price(base.getPrice())
                .estimatedDeliveryDate(delivery)
                .averageRating(averageRating)
                .copiesSold(base.getCopiesSold())
                .build();
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Fetch average ratings for a list of book IDs in a single GROUP BY query.
     * Returns an empty map when the list is empty.
     * Books absent from the result have no reviews (callers treat them as null).
     */
    private Map<Long, Double> batchFetchRatings(List<Long> bookIds) {
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> rows = reviewRepository.findAverageRatingsByBookIds(bookIds);
        Map<Long, Double> map = new HashMap<>(rows.size() * 2);
        for (Object[] row : rows) {
            Long   id     = ((Number) row[0]).longValue();
            Double rating = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            map.put(id, rating);
        }
        return map;
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sortBy.toLowerCase()) {
            case "price_asc"   -> Sort.by(Sort.Direction.ASC,  "price");
            case "price_desc"  -> Sort.by(Sort.Direction.DESC, "price");
            case "newest"      -> Sort.by(Sort.Direction.DESC, "publishedDate");
            case "bestseller"  -> Sort.by(Sort.Direction.DESC, "copiesSold");
            default            -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "…";
    }
}
