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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookService#findAll} covering every filter combination
 * and verifying the batch rating query behaviour.
 *
 * Uses Mockito: no Spring context, no database — fast and deterministic.
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

    private Author  author;
    private Genre   genre;
    private Book    book1;
    private Book    book2;

    @BeforeEach
    void setUp() {
        author = Author.builder().name("Test Author").build();

        genre = Genre.builder().name("Fiction").slug("fiction").build();

        book1 = Book.builder()
                .title("Clean Code")
                .description("A book about writing clean code.")
                .format(BookFormat.PAPERBACK)
                .price(new BigDecimal("299.00"))
                .language("English")
                .author(author)
                .isActive(true)
                .build();
        // set ID reflectively via the base class setter
        book1.setId(1L);

        book2 = Book.builder()
                .title("Design Patterns")
                .description("Gang of Four design patterns.")
                .format(BookFormat.HARDCOVER)
                .price(new BigDecimal("499.00"))
                .language("English")
                .author(author)
                .isActive(true)
                .build();
        book2.setId(2L);

        // Default stubs used by most tests
        when(deliveryDateCalculator.calculate()).thenReturn(LocalDate.now().plusDays(3));
        when(bookMapper.toSummaryResponse(book1)).thenReturn(
                BookSummaryResponse.builder()
                        .id(1L).title("Clean Code").price(new BigDecimal("299.00"))
                        .format(BookFormat.PAPERBACK).copiesSold(0).build());
        when(bookMapper.toSummaryResponse(book2)).thenReturn(
                BookSummaryResponse.builder()
                        .id(2L).title("Design Patterns").price(new BigDecimal("499.00"))
                        .format(BookFormat.HARDCOVER).copiesSold(0).build());

        // Default: no reviews for any book
        when(reviewRepository.findAverageRatingsByBookIds(anyCollection()))
                .thenReturn(Collections.emptyList());
    }

    // ── Helper ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Page<Book> stubRepositoryPage(Book... books) {
        Page<Book> page = new PageImpl<>(List.of(books));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        return page;
    }

    // ── Tests ───────────────────────────────────────────────────────────────────

    @Test
    void noFilters_returnsAllActiveBooks() {
        stubRepositoryPage(book1, book2);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(BookSummaryResponse::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Design Patterns");
        // Specification should have been passed to the repository
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFilter_passesSearchTermToSpecification() {
        stubRepositoryPage(book1);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, "Clean", null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
        // Genre repo must NOT be called when only search is provided
        verifyNoInteractions(genreRepository);
    }

    @Test
    void genreFilter_resolvesSlugAndPassesGenreIdToSpecification() {
        genre.setId(10L);
        when(genreRepository.findBySlug("fiction")).thenReturn(Optional.of(genre));
        stubRepositoryPage(book1);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, "fiction", null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(genreRepository).findBySlug("fiction");
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void genreFilter_unknownSlug_returnsEmptyPage() {
        when(genreRepository.findBySlug("unknown-genre")).thenReturn(Optional.empty());
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, "unknown-genre", null, null, null, null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void genreFilter_allSlug_isIgnored() {
        stubRepositoryPage(book1, book2);

        bookService.findAll(0, 20, null, "all", null, null, null, null, null);

        // "all" should not trigger a genre lookup
        verifyNoInteractions(genreRepository);
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void languageFilter_passesLanguageToSpecification() {
        stubRepositoryPage(book1);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, "English", null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
        verifyNoInteractions(genreRepository);
    }

    @Test
    void languageFilter_allValue_isIgnored() {
        stubRepositoryPage(book1, book2);

        bookService.findAll(0, 20, null, null, "All", null, null, null, null);

        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void formatFilter_passesFormatToSpecification() {
        stubRepositoryPage(book1);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, "PAPERBACK", null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void formatFilter_unknownFormatValue_treatedAsNoFilter() {
        stubRepositoryPage(book1, book2);

        // Should not throw; unknown format is silently ignored
        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, "INVALID_FORMAT", null, null, null);

        assertThat(result.getContent()).hasSize(2);
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void priceRangeFilter_passesMinAndMaxToSpecification() {
        stubRepositoryPage(book1);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, null,
                        new BigDecimal("100"), new BigDecimal("400"), null);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void combinedFilters_allPassedToSpecificationTogether() {
        genre.setId(10L);
        when(genreRepository.findBySlug("fiction")).thenReturn(Optional.of(genre));
        stubRepositoryPage(book1);

        // All filters active simultaneously — the old if/else chain would only honour the first
        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20,
                        "Clean",       // search
                        "fiction",     // genreSlug
                        "English",     // language
                        "PAPERBACK",   // format
                        new BigDecimal("100"),  // minPrice
                        new BigDecimal("400"),  // maxPrice
                        "price_asc");

        assertThat(result.getContent()).hasSize(1);
        // Genre slug IS resolved even when search is also active
        verify(genreRepository).findBySlug("fiction");
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void pagination_pageAndSizeAreForwardedToRepository() {
        Page<Book> thirdPage = new PageImpl<>(List.of(book1), PageRequest.of(2, 5), 11);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(thirdPage);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(2, 5, null, null, null, null, null, null, null);

        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(11);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void averageRatings_batchFetchedInSingleQuery_notPerBook() {
        stubRepositoryPage(book1, book2);

        // Simulate: book1 has rating 4.5, book2 has no reviews
        List<Object[]> ratingRows = new java.util.ArrayList<>();
        ratingRows.add(new Object[]{1L, 4.5});
        when(reviewRepository.findAverageRatingsByBookIds(anyCollection()))
                .thenReturn(ratingRows);

        PagedResponse<BookSummaryResponse> result =
                bookService.findAll(0, 20, null, null, null, null, null, null, null);

        // Batch query called exactly once for the whole page — not per book
        verify(reviewRepository, times(1)).findAverageRatingsByBookIds(anyCollection());
        // Single-book method must NOT be called from findAll
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());

        // book1 gets rating 4.5
        BookSummaryResponse summary1 = result.getContent().stream()
                .filter(b -> b.getId().equals(1L)).findFirst().orElseThrow();
        assertThat(summary1.getAverageRating()).isEqualTo(4.5);

        // book2 gets null (no reviews)
        BookSummaryResponse summary2 = result.getContent().stream()
                .filter(b -> b.getId().equals(2L)).findFirst().orElseThrow();
        assertThat(summary2.getAverageRating()).isNull();
    }

    @Test
    void averageRatings_emptyPage_batchQueryNotCalled() {
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookService.findAll(0, 20, null, null, null, null, null, null, null);

        // No books → no rating query needed
        verify(reviewRepository, never()).findAverageRatingsByBookIds(anyCollection());
        verify(reviewRepository, never()).findAverageRatingByBookId(anyLong());
    }
}
