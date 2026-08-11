package com.bookstore.repository;

import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.domain.enums.BookFormat;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JPA {@link Specification} builder for the book catalogue.
 *
 * All conditions are combined with AND.  {@code isActive = true} is always applied.
 * Call {@link #of} with the desired filter values (nulls are ignored) to build a
 * ready-to-use specification that can be passed directly to
 * {@link BookRepository#findAll(Specification, org.springframework.data.domain.Pageable)}.
 */
public final class BookSpecification {

    private BookSpecification() {}

    /**
     * Build a {@link Specification} that matches active books satisfying all
     * non-null/non-blank filter parameters simultaneously.
     *
     * @param search    title or author name substring (case-insensitive), or {@code null}
     * @param genreId   genre primary key, or {@code null}
     * @param language  language string (case-insensitive), or {@code null}
     * @param format    book format enum value, or {@code null}
     * @param minPrice  minimum price (inclusive), or {@code null}
     * @param maxPrice  maximum price (inclusive), or {@code null}
     */
    public static Specification<Book> of(
            String search,
            Long genreId,
            String language,
            BookFormat format,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory: only show active books
            predicates.add(cb.isTrue(root.get("isActive")));

            // Full-text search: title OR author name (LIKE, case-insensitive)
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Join<Object, Object> author = root.join("author", JoinType.INNER);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(author.get("name")), pattern)
                ));
                // Avoid duplicate rows from the author join
                query.distinct(true);
            }

            // Genre filter via the Many-to-Many join
            if (genreId != null) {
                Join<Book, Genre> genres = root.join("genres", JoinType.INNER);
                predicates.add(cb.equal(genres.get("id"), genreId));
                query.distinct(true);
            }

            // Language filter (case-insensitive)
            if (language != null && !language.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("language")),
                        language.trim().toLowerCase()
                ));
            }

            // Format filter
            if (format != null) {
                predicates.add(cb.equal(root.get("format"), format));
            }

            // Price range (both bounds are applied only when both are provided)
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
