package com.bookstore.domain;

import com.bookstore.domain.enums.BookFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The central product entity representing a single book edition.
 * Maps to the {@code books} table.
 *
 * Notes:
 * - {@code searchVector} is a PostgreSQL {@code tsvector} column maintained by a DB trigger.
 *   It is mapped as insertable=false/updatable=false so Hibernate never tries to write it.
 * - Genres are mapped Many-to-Many via the {@code book_genres} join table.
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Size(max = 20)
    @Column(name = "isbn", length = 20)
    private String isbn;

    @NotBlank
    @Size(max = 50)
    @Column(name = "language", nullable = false, length = 50)
    @Builder.Default
    private String language = "English";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private BookFormat format;

    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column(name = "copies_sold", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int copiesSold = 0;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * PostgreSQL tsvector for full-text search.
     * Never written by Hibernate — managed by a PostgreSQL trigger.
     * Mapped as TEXT so H2 (test) and PostgreSQL both accept the column.
     * The real tsvector index is created by a separate DB migration, not by Hibernate DDL.
     */
    @Column(name = "search_vector", columnDefinition = "TEXT",
            insertable = false, updatable = false)
    private String searchVector;

    // ── Relationships ─────────────────────────────────────────────────────────

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_books_author"))
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id",
            foreignKey = @ForeignKey(name = "fk_books_publisher"))
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_genres",
            joinColumns        = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"),
            foreignKey        = @ForeignKey(name = "fk_book_genres_book"),
            inverseForeignKey = @ForeignKey(name = "fk_book_genres_genre")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
