package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * A single book entry in a user's wishlist.
 * Maps to the {@code wishlist_items} table.
 * The unique constraint prevents the same book appearing twice in one wishlist.
 */
@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_items_wishlist_book",
                columnNames = {"wishlist_id", "book_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem extends BaseCreatedEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_items_wishlist"))
    private Wishlist wishlist;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_items_book"))
    private Book book;
}
