package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * A customer review (rating + text) for a specific book.
 * Maps to the {@code reviews} table.
 * Unique constraint enforces one review per user per book.
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reviews_book_user",
                columnNames = {"book_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseCreatedEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_book"))
    private Book book;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_user"))
    private User user;

    @NotNull
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 100, message = "Review text must not exceed 100 characters")
    @Column(name = "review_text", length = 100)
    private String reviewText;
}
