package com.bookstore.service;

import com.bookstore.common.response.PagedResponse;
import com.bookstore.common.util.DeliveryDateCalculator;
import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.domain.enums.BookFormat;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.GenreRepository;
import com.bookstore.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookService}.
 *
 * Verifies:
 * - batch rating query is used (not per-book) in findAll and findRelated
 * - books with no reviews receive null averageRating
 * - empty page does not trigger the batch rating query
 * - single-book detail still uses findAverageRatingByBookId
 * - batchFetchRatings handles an empty list safely
 * - pagination is forwarded correctly
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookServiceTest {

    @Mock private BookRepository         bookRepository;
    @Mock private GenreRepository        genreRepository;
    @Mock private ReviewRepository       reviewRepository;
    @Mock private BookMapper             bookMapper;
    @Mock private DeliveryDateCalculator deliveryDateCalculator;

    @InjectMocks
    private BookService bookService;

    private Author author;
    private Book   book1;
    private Book   book2;

    @BeforeEach
    void setUp() {
        author = Author.builder().name("Author One").build();

        book1 = Book.builder()
                .title("Clean Code")
                .description("Writing clean code.")
                .format(BookFormat.PAPERBACK)
                .price(new BigDecimal("299.00"))
                .language("English")
                .author(author)
                .isActive(true)
                .build();
        book1.setId(1L);

        book2 = Book.builder()
                .title("Refactoring")
                .description("Improving existing code.")
                .format(BookFormat.HARDCOVER)
                .price(new BigDecimal("499.00"))
                .language("English")
                .author(author)
                .isActive(true)
                .build();
        book2.setId(2L);

        // Default stubs
        when(deliveryDateCalculator.calculate()).thenReturn(LocalDate.now().plusDays(7));
        when(bookMapper.toSummaryResponse(book1)).thenReturn(
                BookSummaryResponse.builder().id(1L).title("Clean Code")
                        .price(new BigDecimal("299.00")).format(BookFormat.PAPERBACK).copiesSold(0).build());
        when(bookMapper.toSummaryResponse(book2)).thenReturn(
                BookSummaryResponse.builder().id(2L).title("Refactoring")
                        .price(new BigDecimal("499.00")).format(BookFormat.HARDCOVER).copiesSold(0).build());
        when(reviewRepository.findAverageRatingsByBookIds(anyCollection()))
                .thenReturn(Collections.emptyList());
    }

    // ── Helper ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void stubPage(Book... books) {
        Page<Book> page = new PageImpl<>(List.of(books));
        when(bookRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(page);
    }

    // ── Catalogue: batch rating queries ────────────────────────────────────────

    @Test
    void findAll_usesBatchRatingQuery_notPerBook() {
        stubPage(book1, book2);

        bookService.findAll(0, 20, null, null, null, null, null, null, null);

        // Batch method called exactly once
        verify(reviewRepository, times(1)).findAverageRatingsByBookIds(anyCollection());
        // Single-book method must NOT be called from findAll
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());
    }

    @Test
    void findAll_booksWithRatings_ratingsApplied() {
        stubPage(book1, book2);

        // book1 has rating 4.5, book2 has no reviews
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1L, 4.5});
        when(reviewRepository.findAverageRatingsByBookIds(anyCollection())).thenReturn(rows);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, null, null, null, null);

        BookSummaryResponse r1 = result.getContent().stream()
                .filter(b -> b.getId().equals(1L)).findFirst().orElseThrow();
        BookSummaryResponse r2 = result.getContent().stream()
                .filter(b -> b.getId().equals(2L)).findFirst().orElseThrow();

        assertThat(r1.getAverageRating()).isEqualTo(4.5);
        assertThat(r2.getAverageRating()).isNull();  // no reviews → null preserved
    }

    @Test
    void findAll_emptyPage_batchQueryNotCalled() {
        when(bookRepository.findByIsActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookService.findAll(0, 20, null, null, null, null, null, null, null);

        // Empty page → no IDs → batch query must not be called
        verify(reviewRepository, never()).findAverageRatingsByBookIds(anyCollection());
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());
    }

    @Test
    void findAll_noFilters_returnsAllBooks() {
        stubPage(book1, book2);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAll_searchFilter_usesSearchRepository() {
        Page<Book> page = new PageImpl<>(List.of(book1));
        when(bookRepository.searchByTitleOrAuthorName(eq("Clean"), any(Pageable.class))).thenReturn(page);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, "Clean", null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).searchByTitleOrAuthorName(eq("Clean"), any(Pageable.class));
        verify(bookRepository, never()).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void findAll_pagination_pageAndSizeForwarded() {
        Page<Book> thirdPage = new PageImpl<>(List.of(book1), PageRequest.of(2, 5), 11);
        when(bookRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(thirdPage);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(2, 5, null, null, null, null, null, null, null);

        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(11);

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findByIsActiveTrue(cap.capture());
        assertThat(cap.getValue().getPageNumber()).isEqualTo(2);
        assertThat(cap.getValue().getPageSize()).isEqualTo(5);
    }

    // ── Related books: batch rating queries ────────────────────────────────────

    @Test
    void findRelated_usesBatchRatingQuery() {
        // Give the author an ID so book1.getAuthor().getId() is non-null
        author.setId(10L);
        when(bookRepository.findByIdAndIsActiveTrue(1L)).thenReturn(java.util.Optional.of(book1));
        when(bookRepository.findRelatedByAuthor(eq(10L), eq(1L), any(Pageable.class)))
                .thenReturn(List.of(book2));

        bookService.findRelated(1L, 6);

        verify(reviewRepository, times(1)).findAverageRatingsByBookIds(anyCollection());
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());
    }

    @Test
    void findRelated_emptyRelatedList_batchQueryNotCalled() {
        author.setId(10L);
        when(bookRepository.findByIdAndIsActiveTrue(1L)).thenReturn(java.util.Optional.of(book1));
        when(bookRepository.findRelatedByAuthor(eq(10L), eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        bookService.findRelated(1L, 6);

        verify(reviewRepository, never()).findAverageRatingsByBookIds(anyCollection());
    }

    // ── Single-book overload (used by HomeService) ─────────────────────────────

    @Test
    void toSummaryWithComputedFields_singleBook_usesSingleRatingQuery() {
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(3.8);

        BookSummaryResponse result = bookService.toSummaryWithComputedFields(book1);

        verify(reviewRepository).findAverageRatingByBookId(1L);
        assertThat(result.getAverageRating()).isEqualTo(3.8);
    }

    @Test
    void toSummaryWithComputedFields_twoArgOverload_usesProvidedRating() {
        BookSummaryResponse result = bookService.toSummaryWithComputedFields(book1, 4.2);

        // Rating repository must NOT be called when rating is pre-fetched
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());
        assertThat(result.getAverageRating()).isEqualTo(4.2);
    }

    @Test
    void toSummaryWithComputedFields_nullRating_preservedAsNull() {
        BookSummaryResponse result = bookService.toSummaryWithComputedFields(book1, null);

        assertThat(result.getAverageRating()).isNull();
    }

    // ── batchFetchRatings: internal unit ───────────────────────────────────────

    @Test
    void batchFetchRatings_emptyList_returnsEmptyMap() {
        Map<Long, Double> result = bookService.batchFetchRatings(Collections.emptyList());

        assertThat(result).isEmpty();
        // Must NOT call the repository when list is empty
        verify(reviewRepository, never()).findAverageRatingsByBookIds(anyCollection());
    }

    @Test
    void batchFetchRatings_withIds_parsesRowsCorrectly() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1L, 4.0});
        rows.add(new Object[]{2L, null});   // book with reviews but null avg (edge case)
        when(reviewRepository.findAverageRatingsByBookIds(anyCollection())).thenReturn(rows);

        Map<Long, Double> result = bookService.batchFetchRatings(List.of(1L, 2L, 3L));

        assertThat(result).containsEntry(1L, 4.0);
        assertThat(result).containsEntry(2L, null);
        assertThat(result).doesNotContainKey(3L);  // book 3 absent → no reviews
    }
}
