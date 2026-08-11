package com.bookstore.service;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.domain.Book;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.HomePageResponse;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for home page sections: Recommended, Bestsellers, New Launches.
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private final BookRepository bookRepository;
    private final BookService    bookService;   // reuses computed-fields mapping

    // ── Full home page ─────────────────────────────────────────────────────────

    /**
     * Return all three home page sections in a single response.
     *
     * @param userId  authenticated user ID, or {@code null} for guests
     * @param limit   number of books per section (capped at {@code MAX_HOME_LIMIT})
     */
    @Transactional(readOnly = true)
    public HomePageResponse getHomePage(Long userId, int limit) {
        limit = Math.min(limit, AppConstants.Pagination.MAX_HOME_LIMIT);
        return HomePageResponse.builder()
                .recommended(getRecommended(userId, limit))
                .bestsellers(getBestsellers(limit))
                .newLaunches(getNewLaunches(limit))
                .build();
    }

    // ── Recommended ────────────────────────────────────────────────────────────

    /**
     * Return personalised recommendations for the authenticated user, or editorial
     * fallback (newest books) for guests and users who follow no authors.
     */
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> getRecommended(Long userId, int limit) {
        limit = Math.min(limit, AppConstants.Pagination.MAX_HOME_LIMIT);
        List<Book> books;

        if (userId != null) {
            Pageable pageable = PageRequest.of(0, limit);
            books = bookRepository.findRecommendedBooksForUser(userId, pageable);
        } else {
            books = null;
        }

        // Fallback: newest books when user is a guest or follows no authors
        if (books == null || books.isEmpty()) {
            books = bookRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        }

        return mapToSummaries(books, limit);
    }

    // ── Bestsellers ────────────────────────────────────────────────────────────

    /**
     * Return the top N books by copies sold.
     */
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> getBestsellers(int limit) {
        limit = Math.min(limit, AppConstants.Pagination.MAX_HOME_LIMIT);
        List<Book> books = bookRepository.findTop10ByIsActiveTrueOrderByCopiesSoldDesc();
        return mapToSummaries(books, limit);
    }

    // ── New Launches ───────────────────────────────────────────────────────────

    /**
     * Return the top N most recently published active books.
     */
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> getNewLaunches(int limit) {
        limit = Math.min(limit, AppConstants.Pagination.MAX_HOME_LIMIT);
        LocalDate since = LocalDate.now().minusMonths(6);
        List<Book> books = bookRepository
                .findTop10ByIsActiveTrueAndPublishedDateGreaterThanEqualOrderByPublishedDateDesc(since);

        // Fallback if nothing published recently
        if (books.isEmpty()) {
            books = bookRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        }

        return mapToSummaries(books, limit);
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private List<BookSummaryResponse> mapToSummaries(List<Book> books, int limit) {
        return books.stream()
                .limit(limit)
                .map(bookService::toSummaryWithComputedFields)
                .collect(Collectors.toList());
    }
}
